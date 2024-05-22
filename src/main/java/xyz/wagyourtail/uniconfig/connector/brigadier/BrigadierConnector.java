package xyz.wagyourtail.uniconfig.connector.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.UniConfig;
import xyz.wagyourtail.uniconfig.connector.brigadier.impl.BrigadierCollectionWrapper;
import xyz.wagyourtail.uniconfig.connector.brigadier.impl.BrigadierListWrapper;
import xyz.wagyourtail.uniconfig.connector.brigadier.impl.BrigadierMapWrapper;
import xyz.wagyourtail.uniconfig.connector.brigadier.impl.BrigadierValueWrapper;

import java.util.*;
import java.util.function.*;

public final class BrigadierConnector {

    private BrigadierConnector() {
    }

    /**
     * for if you don't actually want to use brigadier for a setting,
     * but have it in the required list on the config.
     */
    public static <T> void empty(Setting<T> setting) {
        setting.connector(new BrigadierSettingConnector<>(setting));
    }

    /**
     * connect a setting to a brigadier wrapper.
     * @param setting
     * @param wrapper
     * @param <T>
     */
    public static <T> void connect(Setting<T> setting, BrigadierWrapper<T, ?> wrapper) {
        setting.connector(new BrigadierSettingConnector<>(setting, wrapper));
    }

    public static <T> BrigadierWrapper<T, ?> value(ArgumentType<T> type) {
        return value(type, Function.identity(), t -> Component.literal(t.toString()));
    }

    public static <T> BrigadierWrapper<T, ?> value(ArgumentType<T> type, Function<T, Component> serializer) {
        return value(type, Function.identity(), serializer);
    }

    public static <T, B> BrigadierWrapper<T, B> value(
            ArgumentType<B> type,
            Function<B, T> deserializer,
            Function<T, Component> serializer
    ) {
        return value(type, deserializer, serializer, null, null);
    }

    public static <T, B> BrigadierWrapper<T, B> value(
            ArgumentType<B> type,
            Function<B, T> deserializer,
            Function<T, Component> serializer,
            @Nullable BiPredicate<CommandContext<SharedSuggestionProvider>, B> filter,
            @Nullable SuggestionProvider<SharedSuggestionProvider> suggestor
    ) {
        return new BrigadierValueWrapper<>(
            type,
            "value",
            deserializer,
            serializer,
            filter,
            suggestor
        );
    }

    public static <T extends Enum<T>> BrigadierWrapper<T, String> enumValue(
        Class<T> enumClass
    ) {
        T[] enumConstants = enumClass.getEnumConstants();
        Set<String> enumNames = new HashSet<>();
        for (T enumConstant : enumConstants) {
            enumNames.add(enumConstant.toString());
        }
        return value(
            StringArgumentType.word(),
            s -> Enum.valueOf(enumClass, s),
            s -> Component.literal(s.toString()),
            (ctx, s) -> enumNames.contains(s),
            (ctx, builder) -> SharedSuggestionProvider.suggest(enumNames, builder)
        );
    }

    public static <T> BrigadierWrapper<List<T>, ?> list(
            BrigadierWrapper<T, ?> entryWrapper,
            Function<List<T>, Component> serializer
    ) {
//        entryWrapper.setWriteKey("value");
        return new BrigadierListWrapper<>(entryWrapper, serializer);
    }

    public static <T, V extends Collection<T>> BrigadierWrapper<V, ?> collection(
            BrigadierWrapper<T, ?> entryWrapper,
            Function<V, Component> serializer
    ) {
//        entryWrapper.setWriteKey("value");
        return new BrigadierCollectionWrapper<>(entryWrapper, serializer);
    }

    public static <K, V> BrigadierWrapper<Map<K, V>, ?> map(
            BrigadierWrapper<K, ?> keyWrapper,
            BrigadierWrapper<V, ?> valueWrapper,
            Function<Map<K, V>, Component> serializer
    ) {
        keyWrapper.setWriteKey("key");
//        valueWrapper.setWriteKey("value");
        return new BrigadierMapWrapper<>(keyWrapper, valueWrapper, serializer);
    }

    /**
     * use the consumers to set permissions or whatever.
     * @param config
     * @param parent
     * @param readArg
     * @param writeArg
     * @param <T>
     */
    public static <T extends SharedSuggestionProvider> void register(UniConfig config, ArgumentBuilder<T, ?> parent, Consumer<ArgumentBuilder<T, ?>> readArg, Consumer<ArgumentBuilder<T, ?>> writeArg) {
        config.flatItems().values().forEach(e -> {
            BrigadierSettingConnector<?> connector = e.getConnector(BrigadierSettingConnector.class);
            if (connector != null && !connector.disabled) {
                ArgumentBuilder<T, ?> arg = connector.createArgumentBuilder((Consumer) readArg, (Consumer) writeArg);
                if (arg != null) {
                    parent.then(arg);
                }
            }
        });
    }

}
