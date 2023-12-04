package xyz.wagyourtail.uniconfig;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.wagyourtail.uniconfig.connector.SettingConnector;
import xyz.wagyourtail.uniconfig.util.Utils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Setting<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Setting.class);

    @ApiStatus.Internal
    public final String name;

    @ApiStatus.Internal
    public final Group group;

    @ApiStatus.Internal
    public final Function<T, Component> textValue;

    @ApiStatus.Internal
    public final Supplier<T> defaultValue;

    @ApiStatus.Internal
    public final ConfigWriter<T> serializer;

    @ApiStatus.Internal
    public final ConfigReader<T> deserializer;

    private T value;

    @ApiStatus.Internal
    protected final Map<Class<? extends SettingConnector<?>>, SettingConnector<T>> connectors = new HashMap<>();
    private final Set<Consumer<T>> onSettingUpdate = new CopyOnWriteArraySet<>();

    public Setting(String name, Group group, Function<T, Component> textValue, Supplier<T> defaultValue, ConfigWriter<T> serializer, ConfigReader<T> deserializer) {
        this.name = name;
        this.group = group;
        this.textValue = textValue;
        this.defaultValue = defaultValue;
        this.serializer = serializer;
        this.deserializer = deserializer;
        reRead();
    }

    @ApiStatus.Experimental
    public void reRead() {
        Config config = group.parentConfig().config;
        if (config.contains(key())) {
            value = deserializer.read(config, key());
        } else {
            value = defaultValue.get();
            if (!group.parentConfig().sparseConfig) {
                write();
            }
        }
    }

    @ApiStatus.Internal
    public <V extends SettingConnector<T>> @Nullable V getConnector(Class<V> connectorClass) {
        return (V) connectors.get(connectorClass);
    }

    public void onSettingUpdate(Consumer<T> onSettingUpdate) {
        this.onSettingUpdate.add(onSettingUpdate);
    }

    public void removeListener(Consumer<T> onSettingUpdate) {
        this.onSettingUpdate.remove(onSettingUpdate);
    }

    public void write() {
        write(true);
    }

    public void write(boolean writeParent) {
        try {
            Config config = group.parentConfig().config;
            serializer.write(config, key(), value);
            if (config instanceof CommentedConfig) {
                String commentKey = String.join(".", translateKeyList()) + ".comment";
                if (Language.getInstance().has(commentKey)) {
                    ((CommentedConfig) config).setComment(key(), Language.getInstance().getOrDefault(commentKey, null));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to write setting {}", nameKey(), e);
        }
        if (writeParent) {
            group.write();
        }
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        if (value.equals(this.value)) return;
        setValueNoSave(value);
        group.parentConfig().save(this);
    }

    @ApiStatus.Experimental
    public void setValueNoSave(T value) {
        if (value.equals(this.value)) return;
        this.value = value;
        onSettingUpdate.forEach(c -> c.accept(value));
    }

    @ApiStatus.Internal
    public List<String> key() {
        List<String> list = group.key();
        list.add(name);
        return list;
    }

    public String nameKey() {
        return String.join(".", key());
    }

    @ApiStatus.Internal
    public List<String> translateKeyList() {
        List<String> key = group.translateKeyList();
        key.add(name);
        return key;
    }

    @ApiStatus.Internal
    public Setting<T> copyTo(Group group) {
        Setting<T> setting = new Setting<>(name, group, textValue, defaultValue, serializer, deserializer);
        setting.value = value;
        for (SettingConnector<T> value : connectors.values()) {
            setting.connector(value.copyTo(setting));
        }
        return setting;
    }

    public MutableComponent name() {
        return Utils.translatable(String.join(".", translateKeyList()));
    }

    @Nullable
    public MutableComponent description() {
        String comment = String.join(".", translateKeyList()) + ".comment";
        if (Language.getInstance().has(comment)) {
            return Utils.translatable(comment);
        }
        return null;
    }

    public Component getTextValue() {
        return textValue.apply(value);
    }

    public MutableComponent toText() {
        return name().append(" -> ").append(textValue.apply(value));
    }

    public void connector(SettingConnector<T> connector) {
        connectors.put(connector.getConnectorClass(), connector);
    }

    @FunctionalInterface
    public interface ConfigWriter<T> {
        void write(Config config, List<String> key, T value);
    }

    @FunctionalInterface
    public interface ConfigReader<T> {
        T read(Config config, List<String> key);
    }

}
