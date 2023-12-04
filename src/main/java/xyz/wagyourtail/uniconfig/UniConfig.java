package xyz.wagyourtail.uniconfig;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.io.function.IOSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.wagyourtail.uniconfig.connector.SettingConnector;
import xyz.wagyourtail.uniconfig.nightconfig.nbt.NbtFormat;
import xyz.wagyourtail.uniconfig.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * base config class, there are several ways to use this, but the most common is to extend it to create a config class with fields for
 * each config item.
 */
public class UniConfig extends Group {
    static final Logger LOGGER = LoggerFactory.getLogger(UniConfig.class);

    static {
        FormatDetector.registerExtension("nbt", NbtFormat.COMPRESSED);
    }

    @ApiStatus.Internal
    public final Config config;
    private final IOSupplier<@Nullable OutputStream> saveStreamer;

    public final Set<Class<? extends SettingConnector>> requiredConnectors = new HashSet<>();

    public final boolean sparseConfig;

    /**
     * constructs without a save path, therefore
     * this should be used for in-memory stuff & bulk changes with {@link #readFrom(Group)}.
     * @param name
     */
    public UniConfig(@NotNull String name) {
        super(name, null);
        this.config = Config.inMemory();
        this.sparseConfig = false;
        this.saveStreamer = () -> null;
    }


    public UniConfig(@NotNull String name, Path path, boolean sparseConfig) {
        super(name, null);
        this.config = tryRead(path);
        this.sparseConfig = sparseConfig;
        this.saveStreamer = () -> Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @ApiStatus.Experimental
    protected UniConfig(@NotNull String name, @NotNull Config config, IOSupplier<@Nullable OutputStream> outputStream, boolean sparseConfig) {
        super(name, null);
        this.config = config;
        this.saveStreamer = outputStream;
        this.sparseConfig = sparseConfig;
    }

    private static Config tryRead(Path path) {
        ConfigFormat<?> config = FormatDetector.detect(path);
        if (config == null) config = TomlFormat.instance();
        if (Files.exists(path)) {
            try (InputStream is = Files.newInputStream(path)) {
                 return config.createParser().parse(is);
            } catch (Throwable e) {
                LOGGER.error("Failed to read config", e);
                try {
                    Files.move(path, path.resolveSibling(path.getFileName() + ".broken"));
                } catch (IOException ex) {
                    LOGGER.error("Failed to move broken config", ex);
                }
            }
        }
        return config.createConfig();
    }

    public void reRead(Path path) {
        reRead(tryRead(path).unmodifiable());
    }

    @ApiStatus.Experimental
    public void reRead(UnmodifiableConfig config) {
        this.config.clear();
        this.config.putAll(config);
        for (Setting<?> setting : flatItems().values()) {
            setting.reRead();
        }
    }

    public void save() {
        save(null);
    }

    public void save(@Nullable Setting<?> item) {
        if (item != null) {
            item.write(true);
        } else {
            writeAll();
        }
        try (OutputStream stream = saveStreamer.get()) {
            if (stream != null) {
                config.configFormat().createWriter().write(config.unmodifiable(), stream);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    public Component toText() {
        MutableComponent text = Utils.translatable(name).append(": ");
        Map<String, Setting<?>> sorted = flatItems();
        for (Setting<?> item : sorted.values()) {
            text = text.append("\n").append(item.toText());
        }
        return text;
    }

}
