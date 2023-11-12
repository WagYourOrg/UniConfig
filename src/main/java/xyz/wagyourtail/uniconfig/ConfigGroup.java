package xyz.wagyourtail.uniconfig;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigGroup {

    public final String name;
    public final @Nullable ConfigGroup parent;
    public final @Nullable String comment;

    @ApiStatus.Internal
    public final Map<String, ConfigSetting<?, ?>> configItems = new HashMap<>();

    @ApiStatus.Internal
    public final Set<ConfigGroup> children = new HashSet<>();

    public ConfigGroup(String name, @Nullable ConfigGroup parent, @Nullable String comment) {
        this.name = name;
        this.parent = parent;
        this.comment = comment;
    }

    public Map<String, ConfigSetting<?, ?>> flatItems() {
        Map<String, ConfigSetting<?, ?>> items = new HashMap<>();
        for (ConfigGroup child : children) {
            items.putAll(child.flatItems());
        }
        items.putAll(configItems);
        return items;
    }

    public UniConfig parentConfig() {
        ConfigGroup current = this;
        while (current.parent != null) {
            if (current instanceof UniConfig) {
                return (UniConfig) current;
            }
            current = current.parent;
        }
        throw new IllegalStateException("No parent UniConfig found");
    }

    public void write() {
        write(true);
    }

    public void write(boolean writeParent) {
        Config config = parentConfig().config;
        if (config instanceof CommentedConfig) {
            ((CommentedConfig) config).setComment(name, comment);
        }
        for (ConfigSetting<?, ?> item : configItems.values()) {
            item.write(false);
        }
        if (writeParent && parent != null) {
            parent.write();
        }
    }

    public void writeAll() {
        write(false);
        for (ConfigGroup child : children) {
            child.writeAll();
        }
    }

    public void save() {
        write();
        parentConfig().save();
    }

    public List<String> key() {
        if (this instanceof UniConfig) {
            return Collections.emptyList();
        } else if (parent != null) {
            List<String> key = parent.key();
            key.add(name);
            return key;
        } else {
            return Collections.singletonList(name);
        }
    }

    public ConfigGroup group(String name, @Nullable String comment) {
        ConfigGroup group = new ConfigGroup(name, this, comment);
        children.add(group);
        return group;
    }

    public <T, S> ConfigSetting<T, S> setting(ConfigSetting<T, S> item) {
        if (configItems.containsKey(item.name)) throw new IllegalArgumentException("ConfigItem with name " + item.name + " already exists");
        Set<Class<? extends AbstractConnector<?>>> required  = new HashSet<>(parentConfig().requiredConnectors);
        required.removeAll(item.connectors.keySet());
        if (!required.isEmpty()) throw new IllegalArgumentException("Missing required connectors: " + required);
        return item;
    }

    public <T, S> ConfigSetting<T, S> setting(ConfigSetting<T, S> item, Consumer<ConfigSetting<T, S>> preRegister) {
        preRegister.accept(item);
        return setting(item);
    }

    public <T> ConfigSetting<T, T> setting(
            String name,
            @Nullable String comment,
            Supplier<T> defaultValue
    ) {
        return setting(
                name,
                comment,
                it -> Component.literal(it.toString()),
                defaultValue
        );
    }

    public <T> ConfigSetting<T, T> setting(
            String name,
            @Nullable String comment,
            Supplier<T> defaultValue,
            Consumer<ConfigSetting<T, T>> preRegister
    ) {
        return setting(
                name,
                comment,
                it -> Component.literal(it.toString()),
                defaultValue,
                preRegister
        );
    }

    public <T> ConfigSetting<T, T> setting(
            String name,
            @Nullable String comment,
            Function<T, Component> textValue,
            Supplier<T> defaultValue
    ) {
        return setting(
                name,
                comment,
                textValue,
                defaultValue,
                Function.identity(),
                Function.identity()
        );
    }

    public <T> ConfigSetting<T, T> setting(
            String name,
            @Nullable String comment,
            Function<T, Component> textValue,
            Supplier<T> defaultValue,
            Consumer<ConfigSetting<T, T>> preRegister
    ) {
        return setting(
                name,
                comment,
                textValue,
                defaultValue,
                Function.identity(),
                Function.identity(),
                preRegister
        );
    }

    public <T, S> ConfigSetting<T, S> setting(
            String name,
            @Nullable String comment,
            Function<T, Component> textValue,
            Supplier<T> defaultValue,
            Function<T, @Nullable S> serializer,
            Function<@Nullable S, T> deserializer
    ) {
        return setting(new ConfigSetting<>(name, this, comment, textValue, defaultValue, serializer, deserializer));
    }

    public <T, S> ConfigSetting<T, S> setting(
            String name,
            @Nullable String comment,
            Function<T, Component> textValue,
            Supplier<T> defaultValue,
            Function<T, @Nullable S> serializer,
            Function<@Nullable S, T> deserializer,
            Consumer<ConfigSetting<T, S>> preRegister
    ) {
        return setting(new ConfigSetting<>(name, this, comment, textValue, defaultValue, serializer, deserializer), preRegister);
    }


}
