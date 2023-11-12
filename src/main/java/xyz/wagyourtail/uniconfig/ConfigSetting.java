package xyz.wagyourtail.uniconfig;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigSetting<T, S> {

    public final String name;
    public final ConfigGroup group;
    public final @Nullable String comment;

    public final Function<T, Component> textValue;

    public final Supplier<T> defaultValue;

    public final Function<T, @Nullable S> serializer;
    public final Function<@Nullable S, T> deserializer;

    public final Map<Class<? extends AbstractConnector<T>>, AbstractConnector<T>> connectors = new HashMap<>();

    private T value;

    public ConfigSetting(String name, ConfigGroup group, @Nullable String comment, Function<T, Component> textValue, Supplier<T> defaultValue, Function<T, @Nullable S> serializer, Function<@Nullable S, T> deserializer) {
        this.name = name;
        this.group = group;
        this.comment = comment;
        this.textValue = textValue;
        this.defaultValue = defaultValue;
        this.serializer = serializer;
        this.deserializer = deserializer;
        value = loadValue();
    }


    private T loadValue() {
        Config config = group.parentConfig().config;
        if (config.contains(key())) {
            return deserializer.apply(config.get(key()));
        } else {
            return defaultValue.get();
        }
    }

    public void write() {
        write(true);
    }

    public void write(boolean writeParent) {
        Config config = group.parentConfig().config;
        config.set(key(), serializer.apply(value));
        if (config instanceof CommentedConfig) {
            ((CommentedConfig) config).setComment(stringKey(), comment);
        }
        if (writeParent) {
            group.write();
        }
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        group.parentConfig().save(this);
    }

    public void setValueNoSave(T value) {
        this.value = value;
    }

    public List<String> key() {
        List<String> list = group.key();
        list.add(name);
        return list;
    }

    public String stringKey() {
        return String.join(".", key());
    }

    public Component toText() {
        return Component.translatable(stringKey()).append(" -> ").append(textValue.apply(value));
    }

    public void connector(AbstractConnector<T> connector) {
        connectors.put(connector.getConnectorClass(), connector);
    }

}
