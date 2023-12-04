package xyz.wagyourtail.uniconfig;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.connector.GroupConnector;
import xyz.wagyourtail.uniconfig.connector.SettingConnector;
import xyz.wagyourtail.uniconfig.util.Utils;

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

    public Group copyTo(Group group) {
        for (Map.Entry<String, Setting<?>> entry : this.configItems.entrySet()) {
            group.configItems.put(entry.getKey(), entry.getValue().copyTo(group));
        }
        for (Group g : this.children) {
            group.children.add(g.copyTo(new Group(g.name, group)));
        }
        for (GroupConnector connector : this.connectors.values()) {
            Setting<?> attached = connector.getAttachedSetting() == null ? null : group.configItems.get(connector.getAttachedSetting().name);
            connector(connector.copyTo(group, attached));
        }
        return group;
    }

    /* HELPERS BELOW THIS POINT */

    public Group group(String name) {
        Group group = new Group(name, this);
        children.add(group);
        return group;
    }

    public Group group(String name, Consumer<Group> preRegister) {
        Group group = new Group(name, this);
        preRegister.accept(group);
        children.add(group);
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
                it -> Component.nullToEmpty(it.toString()),
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
                Function.identity(),
                e -> e == null ? defaultValue : e
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
                Function.identity(),
                e -> e == null ? defaultValue : e,
                preRegister
        );
    }


    public <T> Setting<T> setting(
            String name,
            T defaultValue,
            Function<T, @NotNull String> serializer,
            Function<@Nullable String, T> deserializer
    ) {
        return setting(
                name,
                it -> Component.nullToEmpty(serializer.apply(it)),
                defaultValue,
                serializer,
                deserializer
        );
    }


    public <T> Setting<T> setting(
            String name,
            T defaultValue,
            Function<T, @NotNull String> serializer,
            Function<@NotNull String, T> deserializer,
            Consumer<Setting<T>> preRegister
    ) {
        return setting(
                name,
                it -> Component.nullToEmpty(serializer.apply(it)),
                defaultValue,
                serializer,
                deserializer,
                preRegister
        );
    }

    public <T, S> Setting<T> setting(
            String name,
            Function<T, Component> textValue,
            T defaultValue,
            Function<T, @NotNull S> serializer,
            Function<@Nullable S, T> deserializer
    ) {
        return setting(new Setting<>(name, this, textValue, () -> defaultValue, (a, b, c) -> a.set(b, serializer.apply(c)), (a, b) -> deserializer.apply(a.get(b))));
    }

    public <T, S> Setting<T> setting(
            String name,
            Function<T, Component> textValue,
            T defaultValue,
            Function<T, @Nullable S> serializer,
            Function<@Nullable S, T> deserializer,
            Consumer<Setting<T>> preRegister
    ) {
        return setting(new Setting<>(name, this, textValue, () -> defaultValue, (a, b, c) -> a.set(b, serializer.apply(c)), (a, b) -> deserializer.apply(a.get(b))), preRegister);
    }

    public <T extends Enum<T>> Setting<T> enumSetting(
            String name,
            T defaultValue
    ) {
        Class<T> enumClass = defaultValue.getDeclaringClass();
        return setting(
                name,
                defaultValue,
                Enum::toString,
                (e) -> e == null ? defaultValue : Enum.valueOf(enumClass, e)
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
                }), preRegister);
    }

}
