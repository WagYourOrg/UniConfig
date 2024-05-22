package xyz.wagyourtail.uniconfig;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.connector.GroupConnector;
import xyz.wagyourtail.uniconfig.connector.SettingConnector;
import xyz.wagyourtail.uniconfig.registry.ComponentFactoryRegistry;
import xyz.wagyourtail.uniconfig.registry.ConfigTypeFactoryRegistry;
import xyz.wagyourtail.uniconfig.util.TranslationUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Group {

    public final String name;

    @ApiStatus.Internal
    public final @Nullable Group parent;

    @ApiStatus.Internal
    public final Map<String, Setting<?>> configItems = new LinkedHashMap<>();

    @ApiStatus.Internal
    public final Set<Group> children = new LinkedHashSet<>();

    @ApiStatus.Internal
    protected final Map<Class<? extends GroupConnector>, GroupConnector> connectors = new HashMap<>();

    @ApiStatus.Internal
    public Group(String name, @Nullable Group parent) {
        this.name = name;
        this.parent = parent;
    }

    @ApiStatus.Internal
    public Map<String, Setting<?>> flatItems() {
        Map<String, Setting<?>> items = new LinkedHashMap<>();
        for (Group child : children) {
            for (Map.Entry<String, Setting<?>> entry : child.flatItems().entrySet()) {
                items.put(child.name + "." + entry.getKey(), entry.getValue());
            }
        }
        items.putAll(configItems);
        return items;
    }

    @ApiStatus.Internal
    public UniConfig parentConfig() {
        Group current = this;
        do {
            if (current instanceof UniConfig) {
                return (UniConfig) current;
            }
            current = current.parent;
        } while (current != null);
        throw new IllegalStateException("No parent UniConfig found");
    }

    public void write() {
        write(true);
    }

    public void write(boolean writeParent) {
        Config config = parentConfig().config;
        if (config instanceof CommentedConfig) {
            String commentKey = String.join(".", translateKeyList()) + ".comment";
            if (Language.getInstance().has(commentKey)) {
                ((CommentedConfig) config).setComment(name, Language.getInstance().getOrDefault(commentKey, null));
            }
        }
        if (writeParent && parent != null) {
            parent.write();
        }
    }

    public void writeAll() {
        write(false);
        for (Group child : children) {
            child.writeAll();
        }
    }

    public void save() {
        write();
        parentConfig().save();
    }

    @ApiStatus.Internal
    public List<String> key() {
        List<String> key = translateKeyList();
        key.remove(0);
        return key;
    }

    public MutableComponent name() {
        return TranslationUtils.translatable(String.join(".", translateKeyList()));
    }

    @Nullable
    public MutableComponent description() {
        String comment = String.join(".", translateKeyList()) + ".comment";
        if (Language.getInstance().has(comment)) {
            return TranslationUtils.translatable(comment);
        }
        return null;
    }

    @ApiStatus.Internal
    public List<String> translateKeyList() {
        if (parent != null) {
            List<String> key = parent.translateKeyList();
            key.add(name);
            return key;
        } else {
            return new ArrayList<>(Collections.singletonList(name));
        }
    }

    @ApiStatus.Experimental
    public <T> Setting<T> setting(Setting<T> item) {
        Set<Class<? extends SettingConnector>> required  = new HashSet<>(parentConfig().requiredConnectors);
        required.removeAll(item.connectors.keySet());
        if (!required.isEmpty()) throw new IllegalArgumentException("Missing required connectors: " + required);
        if (configItems.containsKey(item.name)) throw new IllegalArgumentException("ConfigItem with name " + item.name + " already exists");
        configItems.put(item.name, item);
        return item;
    }

    @ApiStatus.Experimental
    public <T> Setting<T> setting(Setting<T> item, Consumer<Setting<T>> preRegister) {
        preRegister.accept(item);
        return setting(item);
    }

    public void connector(GroupConnector connector) {
        connectors.put(connector.getConnectorClass(), connector);
    }

    @ApiStatus.Internal
    public <V extends GroupConnector> @Nullable V getConnector(Class<V> connectorClass) {
        return (V) connectors.get(connectorClass);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void readFrom(Group other) {
        Map<String, Setting<?>> flatItems = flatItems();
        // ensure all in other exist in this
        for (Map.Entry<String, Setting<?>> item : other.flatItems().entrySet()) {
            if (!flatItems.containsKey(item.getKey())) {
                throw new IllegalArgumentException("Cannot copy item " + item.getKey() + " from " + other.name + " to " + name + " as it does not exist");
            }
        }
        for (Map.Entry<String, Setting<?>> item : other.flatItems().entrySet()) {
            Setting thisItem = flatItems.get(item.getKey());
            thisItem.setValueNoSave(item.getValue().getValue());
        }
        save();
    }

    public void copyTo(Group group) {
        for (Map.Entry<String, Setting<?>> entry : this.configItems.entrySet()) {
            group.configItems.put(entry.getKey(), entry.getValue().copyTo(group));
        }
        for (Group g : this.children) {
            Group child = new Group(g.name, group);
            group.children.add(child);
            g.copyTo(child);
        }
        if (group.parent != null) {
            for (GroupConnector connector : this.connectors.values()) {
                Setting<?> attached;
                if (connector.getAttachedSetting() == null) {
                    attached = null;
                } else {
                    attached = group.parent.configItems.get(connector.getAttachedSetting().name);
                    if (attached == null) {
                        throw new IllegalStateException("attached setting " + connector.getAttachedSetting().name + " not found in group " + group.name);
                    }
                }
                group.connector(connector.copyTo(group, attached));
            }
        }
    }

    public void group(Group group) {
        children.add(group);
    }

    /* HELPERS BELOW THIS POINT */

    public Group group(String name) {
        Group group = new Group(name, this);
        children.add(group);
        return group;
    }

    public Group group(String name, Consumer<Group> postRegister) {
        Group group = new Group(name, this);
        children.add(group);
        postRegister.accept(group);
        return group;
    }

    public <T> Setting<T> setting(
            String name,
            T defaultValue
    ) {
        return setting(
                name,
                it -> Component.nullToEmpty(it.toString()),
                defaultValue
        );
    }

    public <T> Setting<T> setting(
            String name,
            T defaultValue,
            Consumer<Setting<T>> preRegister
    ) {
        return setting(
                name,
                (Function<T, Component>) ComponentFactoryRegistry.DEFAULT,
                defaultValue,
                preRegister
        );
    }

    public <T> Setting<T> setting(
            String name,
            Function<T, Component> textValue,
            T defaultValue
    ) {
        return setting(
                name,
                textValue,
                defaultValue,
                (ConfigTypeFactoryRegistry.ConfigType<T>) ConfigTypeFactoryRegistry.DEFAULT
        );
    }

    public <T> Setting<T> setting(
            String name,
            Function<T, Component> textValue,
            T defaultValue,
            Consumer<Setting<T>> preRegister
    ) {
        return setting(
                name,
                textValue,
                defaultValue,
                (ConfigTypeFactoryRegistry.ConfigType<T>) ConfigTypeFactoryRegistry.DEFAULT,
                preRegister
        );
    }


    public <T> Setting<T> setting(
            String name,
            T defaultValue,
            ConfigTypeFactoryRegistry.ConfigType<T> serializer
    ) {
        return setting(
                name,
                (Function<T, Component>) ComponentFactoryRegistry.DEFAULT,
                defaultValue,
                serializer
        );
    }


    public <T> Setting<T> setting(
            String name,
            T defaultValue,
            ConfigTypeFactoryRegistry.ConfigType<T> serializer,
            Consumer<Setting<T>> preRegister
    ) {
        return setting(
                name,
                (Function<T, Component>) ComponentFactoryRegistry.DEFAULT,
                defaultValue,
                serializer,
                preRegister
        );
    }

    public <T> Setting<T> setting(
            String name,
            Function<T, Component> textValue,
            T defaultValue,
            ConfigTypeFactoryRegistry.ConfigType<T> serializer
    ) {
        return setting(new Setting<>(name, this, textValue, () -> defaultValue, serializer));
    }

    public <T> Setting<T> setting(
            String name,
            Function<T, Component> textValue,
            T defaultValue,
            ConfigTypeFactoryRegistry.ConfigType<T> serializer,
            Consumer<Setting<T>> preRegister
    ) {
        return setting(new Setting<>(name, this, textValue, () -> defaultValue, serializer), preRegister);
    }

    public <T extends Enum<T>> Setting<T> enumSetting(
            String name,
            T defaultValue
    ) {
        Class<T> enumClass = defaultValue.getDeclaringClass();
        return setting(
                name,
                defaultValue,
                ConfigTypeFactoryRegistry.ConfigType.of(
                    (config, key, value) -> config.set(key, value.name()),
                    (config, key) -> Enum.valueOf(enumClass, config.get(key))
                )
        );
    }

    public <V> Setting<Map<String, V>> mapSetting(
            String name,
            Map<String, V> defaultValue
    ) {
        return mapSetting(
                name,
                defaultValue,
                e -> {
                    MutableComponent text = Component.literal("");
                    for (Map.Entry<String, V> entry : e.entrySet()) {
                        text.append(Component.literal(entry.getKey() + ": ")).append(Component.literal(entry.getValue().toString())).append(Component.literal("\n"));
                    }
                    return text;
                },
                HashMap::new,
                Function.identity(),
                Function.identity()
        );
    }

    public <V> Setting<Map<String, V>> mapSetting(
            String name,
            Map<String, V> defaultValue,
            Consumer<Setting<Map<String, V>>> preRegister
    ) {
        return mapSetting(
                name,
                defaultValue,
                e -> {
                    MutableComponent text = Component.literal("");
                    for (Map.Entry<String, V> entry : e.entrySet()) {
                        text.append(Component.literal(entry.getKey() + ": ")).append(Component.literal(entry.getValue().toString())).append(Component.literal("\n"));
                    }
                    return text;
                },
                HashMap::new,
                Function.identity(),
                Function.identity(),
                preRegister
        );
    }

    public <V> Setting<Map<String, V>> mapSetting(
            String name,
            Map<String, V> defaultValue,
            Function<Map<String, V>, Map<String, V>> copyMutable
    ) {
        return mapSetting(
                name,
                defaultValue,
                e -> {
                    MutableComponent text = Component.literal("");
                    for (Map.Entry<String, V> entry : e.entrySet()) {
                        text.append(Component.literal(entry.getKey() + ": ")).append(Component.literal(entry.getValue().toString())).append(Component.literal("\n"));
                    }
                    return text;
                },
                copyMutable,
                Function.identity(),
                Function.identity()
        );
    }

    public <V> Setting<Map<String, V>> mapSetting(
            String name,
            Map<String, V> defaultValue,
            Function<Map<String, V>, Map<String, V>> copyMutable,
            Consumer<Setting<Map<String, V>>> preRegister
    ) {
        return mapSetting(
                name,
                defaultValue,
                e -> {
                    MutableComponent text = Component.literal("");
                    for (Map.Entry<String, V> entry : e.entrySet()) {
                        text.append(Component.literal(entry.getKey() + ": ")).append(Component.literal(entry.getValue().toString())).append(Component.literal("\n"));
                    }
                    return text;
                },
                copyMutable,
                Function.identity(),
                Function.identity(),
                preRegister
        );
    }

    public <V> Setting<Map<String, V>> mapSetting(
            String name,
            Map<String, V> defaultValue,
            Function<Map<String, V>, Component> textValue,
            Function<Map<String, V>, Map<String, V>> copyMutable
    ) {
        return mapSetting(
            name,
            defaultValue,
            textValue,
            copyMutable,
            Function.identity(),
            Function.identity()
        );
    }

    public <V> Setting<Map<String, V>> mapSetting(
            String name,
            Map<String, V> defaultValue,
            Function<Map<String, V>, Component> textValue,
            Function<Map<String, V>, Map<String, V>> copyMutable,
            Consumer<Setting<Map<String, V>>> preRegister
    ) {
        return mapSetting(
                name,
                defaultValue,
                textValue,
                copyMutable,
                Function.identity(),
                Function.identity(),
                preRegister
        );
    }

    public <V, S> Setting<Map<String, V>> mapSetting(
            String name,
            Map<String, V> defaultValue,
            Function<Map<String, V>, Component> textValue,
            Function<Map<String, V>, Map<String, V>> copyMutable,
            Function<@Nullable S, V> valueDeserializer,
            Function<V, @Nullable S> valueSerializer
    ) {
        return setting(new Setting<>(
                name,
                this,
                textValue,
                () -> copyMutable.apply(defaultValue),
                ConfigTypeFactoryRegistry.ConfigType.of(
                    (a, b, c) -> {
                        b.add("");
                        for (Map.Entry<String, V> entry : c.entrySet()) {
                            b.remove(b.size() - 1);
                            b.add(entry.getKey());
                            a.set(b, valueSerializer.apply(entry.getValue()));
                        }
                    },
                    (a, b) -> {
                        Map<String, V> map = new HashMap<>();
                        Config config = a.get(b);
                        for (Config.Entry entry : config.entrySet()) {
                            map.put(entry.getKey(), valueDeserializer.apply(entry.getValue()));
                        }
                        return map;
                    }
                )
        ));
    }

    public <V, S> Setting<Map<String, V>> mapSetting(
            String name,
            Map<String, V> defaultValue,
            Function<Map<String, V>, Component> textValue,
            Function<Map<String, V>, Map<String, V>> copyMutable,
            Function<@Nullable S, V> valueDeserializer,
            Function<V, @Nullable S> valueSerializer,
            Consumer<Setting<Map<String, V>>> preRegister
    ) {
        return setting(new Setting<>(
                name,
                this,
                textValue,
                () -> copyMutable.apply(defaultValue),
                ConfigTypeFactoryRegistry.ConfigType.of(
                    (a, b, c) -> {
                        b.add("");
                        for (Map.Entry<String, V> entry : c.entrySet()) {
                            b.remove(b.size() - 1);
                            b.add(entry.getKey());
                            a.set(b, valueSerializer.apply(entry.getValue()));
                        }
                    },
                    (a, b) -> {
                        Map<String, V> map = new HashMap<>();
                        Config config = a.get(b);
                        for (Config.Entry entry : config.entrySet()) {
                            map.put(entry.getKey(), valueDeserializer.apply(entry.getValue()));
                        }
                        return map;
                    }
                )
        ), preRegister);
    }

    public <T> Setting<List<T>> listSetting(
        String name,
        List<T> defaultValue
    ) {
        return collectionSetting(
                name,
                defaultValue,
                ArrayList::new
        );
    }
    public <T> Setting<List<T>> listSetting(
            String name,
            List<T> defaultValue,
            Consumer<Setting<List<T>>> preRegister
    ) {
        return collectionSetting(
                name,
                defaultValue,
                ArrayList::new,
                preRegister
        );
    }

    public <T> Setting<Set<T>> setSetting(
            String name,
            Set<T> defaultValue
    ) {
        return collectionSetting(
                name,
                defaultValue,
                HashSet::new
        );
    }
    public <T> Setting<Set<T>> setSetting(
            String name,
            Set<T> defaultValue,
            Consumer<Setting<Set<T>>> preRegister
    ) {
        return collectionSetting(
                name,
                defaultValue,
                HashSet::new,
                preRegister
        );
    }

    public <V, T extends Collection<V>> Setting<T> collectionSetting(
            String name,
            T defaultValue,
            Function<Collection<? extends V>, T> copyMutable
    ) {
        return collectionSetting(
                name,
                defaultValue,
                l -> {
                    MutableComponent text = Component.literal("");
                    for (V v : l) {
                        text.append(Component.literal(v.toString())).append(Component.literal("\n"));
                    }
                    return text;
                },
                copyMutable,
                Function.identity(),
                Function.identity()
        );
    }

    public <V, T extends Collection<V>> Setting<T> collectionSetting(
            String name,
            T defaultValue,
            Function<Collection<? extends V>, T> copyMutable,
            Consumer<Setting<T>> preRegister
    ) {
        return collectionSetting(
                name,
                defaultValue,
                l -> {
                    MutableComponent text = Component.literal("");
                    for (V v : l) {
                        text.append(Component.literal(v.toString())).append(Component.literal("\n"));
                    }
                    return text;
                },
                copyMutable,
                Function.identity(),
                Function.identity(),
                preRegister
        );
    }

    public <V, S, T extends Collection<V>> Setting<T> collectionSetting(
            String name,
            T defaultValue,
            Function<T, Component> textValue,
            Function<Collection<? extends V>, T> copyMutable
    ) {
        return collectionSetting(
                name,
                defaultValue,
                textValue,
                copyMutable,
                Function.identity(),
                Function.identity()
        );
    }

    public <V, S, T extends Collection<V>> Setting<T> collectionSetting(
            String name,
            T defaultValue,
            Function<T, Component> textValue,
            Function<Collection<? extends V>, T> copyMutable,
            Consumer<Setting<T>> preRegister
    ) {
        return collectionSetting(
                name,
                defaultValue,
                textValue,
                copyMutable,
                Function.identity(),
                Function.identity(),
                preRegister
        );
    }

    public <V, S, T extends Collection<V>> Setting<T> collectionSetting(
            String name,
            T defaultValue,
            Function<T, Component> textValue,
            Function<Collection<? extends V>, T> copyMutable,
            Function<@Nullable S, V> valueDeserializer,
            Function<V, @Nullable S> valueSerializer
    ) {
        return setting(new Setting<>(
                name,
                this,
                textValue,
                () -> copyMutable.apply(defaultValue),
                ConfigTypeFactoryRegistry.ConfigType.of(
                    (a, b, c) -> {
                        List<S> list = new ArrayList<>();
                        for (V v : c) {
                            list.add(valueSerializer.apply(v));
                        }
                        a.set(b, list);
                    },
                    (a, b) -> {
                        List<S> list = a.get(b);
                        T collection = copyMutable.apply(Collections.emptyList());
                        for (S s : list) {
                            collection.add(valueDeserializer.apply(s));
                        }
                        return collection;
                    })
                )
        );
    }

    public <V, S, T extends Collection<V>> Setting<T> collectionSetting(
            String name,
            T defaultValue,
            Function<T, Component> textValue,
            Function<Collection<? extends V>, T> copyMutable,
            Function<@Nullable S, V> valueDeserializer,
            Function<V, @Nullable S> valueSerializer,
            Consumer<Setting<T>> preRegister
    ) {
        return setting(new Setting<>(
                name,
                this,
                textValue,
                () -> copyMutable.apply(defaultValue),
                ConfigTypeFactoryRegistry.ConfigType.of(
                    (a, b, c) -> {
                        List<S> list = new ArrayList<>();
                        for (V v : c) {
                            list.add(valueSerializer.apply(v));
                        }
                        a.set(b, list);
                    },
                    (a, b) -> {
                        List<S> list = a.get(b);
                        T collection = copyMutable.apply(Collections.emptyList());
                        for (S s : list) {
                            collection.add(valueDeserializer.apply(s));
                        }
                        return collection;
                    }
                )
        ), preRegister);
    }

}
