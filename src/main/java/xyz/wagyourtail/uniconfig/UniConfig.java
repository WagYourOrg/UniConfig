package xyz.wagyourtail.uniconfig;

import com.electronwill.nightconfig.core.Config;
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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * base config class, there are several ways to use this, but the most common is to extend it to create a config class with fields for
 * each config item.
 */
public class UniConfig extends ConfigGroup {
    static final Logger LOGGER = LoggerFactory.getLogger(UniConfig.class);

    @ApiStatus.Internal
    public final Config config;
    private final IOSupplier<@Nullable OutputStream> saveStreamer;

    public final Set<Class<? extends AbstractConnector<?>>> requiredConnectors = new HashSet<>();

    public UniConfig(@NotNull String name, Path path) {
        super(name, null, null);
        Config config = FormatDetector.detect(path).createConfig();
        if (config == null) config = TomlFormat.newConfig();
        this.config = config;
        this.saveStreamer = () -> Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    @ApiStatus.Internal
    protected UniConfig(@NotNull String name, @NotNull Config config, IOSupplier<@Nullable OutputStream> outputStream) {
        super(name, null, null);
        this.config = config;
        this.saveStreamer = outputStream;
    }

    public void save() {
        save(null);
    }

    public void save(@Nullable ConfigSetting<?, ?> item) {
        if (item != null) {
            item.write(false);
        } else {
            writeAll();
        }
        try (OutputStream stream = saveStreamer.get()) {
            config.configFormat().createWriter().write(config.unmodifiable(), stream);
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    public Component toText() {
        MutableComponent text = Component.translatable(name).append(": ");
        SortedMap<String, ConfigSetting<?, ?>> sorted = new TreeMap<>(flatItems());
        for (ConfigSetting<?, ?> item : sorted.values()) {
            text = text.append("\n").append(item.toText());
        }
        return text;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void copyFrom(UniConfig other) {
        if (this.getClass() != other.getClass()) throw new IllegalArgumentException("Cannot copy from different config type");
        for (Map.Entry<String, ConfigSetting<?, ?>> item : other.flatItems().entrySet()) {
            ConfigSetting thisItem = flatItems().get(item.getKey());
            if (thisItem != null) {
                thisItem.setValueNoSave(item.getValue().getValue());
            }
        }
        save();
    }

}
