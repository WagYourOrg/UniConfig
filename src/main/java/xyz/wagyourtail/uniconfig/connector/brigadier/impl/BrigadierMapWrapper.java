package xyz.wagyourtail.uniconfig.connector.brigadier.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import xyz.wagyourtail.uniconfig.connector.brigadier.BrigadierWrapper;
import xyz.wagyourtail.uniconfig.PlatformMethods;
import xyz.wagyourtail.uniconfig.util.Utils;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BrigadierMapWrapper<K, V, BK, BV> implements BrigadierWrapper<Map<K, V>, Object> {
    private final BrigadierWrapper<K, BK> keyWrapper;
    private final BrigadierWrapper<V, BV> valueWrapper;
    private final Function<Map<K, V>, Component> serializer;

    public BrigadierMapWrapper(BrigadierWrapper<K, BK> keyWrapper, BrigadierWrapper<V, BV> valueWrapper, Function<Map<K, V>, Component> serializer) {
        this.keyWrapper = keyWrapper;
        this.valueWrapper = valueWrapper;
        this.serializer = serializer;
    }

    @Override
    public ArgumentType<Object> getType() {
        throw new UnsupportedOperationException("can't chain");
    }

    @Override
    public String writeKey() {
        throw new UnsupportedOperationException("can't chain");
    }

    @Override
    public void setWriteKey(String writeKey) {
        throw new UnsupportedOperationException("can't chain");
    }

    @Override
    public Map<K, V> parse(Component broadcastName, String valueName, CommandContext<SharedSuggestionProvider> context) {
        throw new UnsupportedOperationException("can't chain");
    }

    @Override
    public Component serialize(Map<K, V> value) {
        return serializer.apply(value);
    }

    @Override
    public void writeBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?>  parent, Consumer<ArgumentBuilder<SharedSuggestionProvider, ?>> writeArgModifier, Function<CommandContext<SharedSuggestionProvider>, Map<K, V>> reader, BiConsumer<Map<K, V>, CommandContext<SharedSuggestionProvider>> writer) {
        // put
        ArgumentBuilder<SharedSuggestionProvider, ?> putArg = LiteralArgumentBuilder.literal("put");
        RequiredArgumentBuilder<SharedSuggestionProvider, BK> keyArg = RequiredArgumentBuilder.argument("key", keyWrapper.getType());
        valueWrapper.writeBuilder(broadcastName, keyArg, writeArgModifier, c -> {
            throw new UnsupportedOperationException();
        }, (value, context) -> {
            Map<K, V> map = reader.apply(context);
            K key = keyWrapper.parse(broadcastName, "key", context);
            if (key != null) {
                map.put(key, value);
                writer.accept(map, context);
                PlatformMethods.INSTANCE.sendFeedback(context.getSource(), Utils.translatable("uniconfig.brigadier.map.put", broadcastName, keyWrapper.serialize(key), valueWrapper.serialize(value)), true);
            }
        });
        if (keyWrapper instanceof BrigadierValueWrapper) {
            if (((BrigadierValueWrapper<K, BK>) keyWrapper).suggestor != null) {
                keyArg.suggests(((BrigadierValueWrapper<K, BK>) keyWrapper).suggestor);
            }
        }
        parent.then(putArg.then(keyArg));
        // remove
        ArgumentBuilder<SharedSuggestionProvider, ?> removeArg = LiteralArgumentBuilder.literal("remove");
        keyWrapper.writeBuilder(broadcastName, removeArg, writeArgModifier, c -> {
            throw new UnsupportedOperationException();
        }, (key, context) -> {
            Map<K, V> map = reader.apply(context);
            map.remove(key);
            writer.accept(map, context);
            PlatformMethods.INSTANCE.sendFeedback(context.getSource(), Utils.translatable("uniconfig.brigadier.map.remove", broadcastName, keyWrapper.serialize(key)), true);
        });
        parent.then(removeArg);
        // clear
        ArgumentBuilder<SharedSuggestionProvider, ?> clearArg = LiteralArgumentBuilder.literal("clear");
        clearArg.executes(context -> {
            Map<K, V> map = reader.apply(context);
            map.clear();
            writer.accept(map, context);
            PlatformMethods.INSTANCE.sendFeedback(context.getSource(), Utils.translatable("uniconfig.brigadier.map.clear", broadcastName), true);
            return Command.SINGLE_SUCCESS;
        });
        writeArgModifier.accept(clearArg);
        parent.then(clearArg);
    }

    @Override
    public void readBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?> readArg, Function<CommandContext<SharedSuggestionProvider>, Map<K, V>> reader) {
        // get
        ArgumentBuilder<SharedSuggestionProvider, ?> getArg = LiteralArgumentBuilder.literal("get");
        keyWrapper.writeBuilder(broadcastName, getArg, a -> {}, c -> {
            throw new UnsupportedOperationException();
        }, (key, context) -> {
            Map<K, V> map = reader.apply(context);
            if (key != null) {
                V value = map.get(key);
                if (value != null) {
                    PlatformMethods.INSTANCE.sendFeedback(context.getSource(), valueWrapper.serialize(value), false);
                }
            }
        });
        readArg.then(getArg);
        // contains
        ArgumentBuilder<SharedSuggestionProvider, ?> containsArg = LiteralArgumentBuilder.literal("contains");
        keyWrapper.writeBuilder(broadcastName, containsArg, a -> {}, c -> {
            throw new UnsupportedOperationException();
        }, (key, context) -> {
            Map<K, V> map = reader.apply(context);
            if (map.containsKey(key)) {
                PlatformMethods.INSTANCE.sendFeedback(context.getSource(), Component.literal("true"), false);
            } else {
                PlatformMethods.INSTANCE.sendFeedback(context.getSource(), Component.literal("false"), false);
            }
        });
        readArg.then(containsArg);
        // size
        ArgumentBuilder<SharedSuggestionProvider, ?> sizeArg = LiteralArgumentBuilder.literal("size");
        sizeArg.executes(context -> {
            Map<K, V> map = reader.apply(context);
            PlatformMethods.INSTANCE.sendFeedback(context.getSource(), Component.literal(Integer.toString(map.size())), false);
            return Command.SINGLE_SUCCESS;
        });
        readArg.then(sizeArg);
        // get all
        readArg.executes(context -> {
            Map<K, V> map = reader.apply(context);
            PlatformMethods.INSTANCE.sendFeedback(context.getSource(), serializer.apply(map), false);
            return Command.SINGLE_SUCCESS;
        });
    }

    @Override
    public void readWriteBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?> readArg, Consumer<ArgumentBuilder<SharedSuggestionProvider, ?>> writeArgModifier, Consumer<Map<K, V>> writer, Supplier<Map<K, V>> reader) {
        writeBuilder(broadcastName, readArg, writeArgModifier, c -> reader.get(), (a, b) -> writer.accept(a));
        readBuilder(broadcastName, readArg, a -> reader.get());
    }
}
