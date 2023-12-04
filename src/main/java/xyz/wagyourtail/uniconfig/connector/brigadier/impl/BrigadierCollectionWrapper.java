package xyz.wagyourtail.uniconfig.connector.brigadier.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.connector.brigadier.BrigadierWrapper;
import xyz.wagyourtail.uniconfig.PlatformMethods;
import xyz.wagyourtail.uniconfig.util.Utils;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BrigadierCollectionWrapper<T, B, V extends Collection<T>> implements BrigadierWrapper<V, Object> {
    protected final BrigadierWrapper<T, B> entryWrapper;
    protected final Function<V, Component> serializer;

    public BrigadierCollectionWrapper(BrigadierWrapper<T, B> entryWrapper, Function<V, Component> serializer) {
        this.entryWrapper = entryWrapper;
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
    public @Nullable V parse(Component broadcastName, String keyVal, CommandContext<SharedSuggestionProvider> context) {
        throw new UnsupportedOperationException("can't chain");
    }

    @Override
    public Component serialize(V value) {
        return serializer.apply(value);
    }

    @Override
    public void writeBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?> parent, Consumer<ArgumentBuilder<SharedSuggestionProvider, ?>> writeArgModifier, Function<CommandContext<SharedSuggestionProvider>, V> reader, BiConsumer<V, CommandContext<SharedSuggestionProvider>> writer) {
        // add
        ArgumentBuilder<SharedSuggestionProvider, ?> addArg = LiteralArgumentBuilder.literal("add");
        entryWrapper.writeBuilder(broadcastName, addArg, writeArgModifier, c -> {
            throw new UnsupportedOperationException();
        }, (value, context) -> {
            V collection = reader.apply(context);
            T entry = entryWrapper.parse(broadcastName, "value", context);
            if (entry != null) {
                collection.add(entry);
                writer.accept(collection, context);
                PlatformMethods.INSTANCE.sendFeedback(context.getSource(), Utils.translatable("uniconfig.brigadier.collection.add", broadcastName, entryWrapper.serialize(entry)), true);
            }
        });
        parent.then(addArg);
        // remove
        ArgumentBuilder<SharedSuggestionProvider, ?> removeArg = LiteralArgumentBuilder.literal("remove");
        entryWrapper.writeBuilder(broadcastName, removeArg, writeArgModifier, c -> {
            throw new UnsupportedOperationException();
        }, (value, context) -> {
            V collection = reader.apply(context);
            T entry = entryWrapper.parse(broadcastName, "value", context);
            if (entry != null) {
                collection.remove(entry);
                writer.accept(collection, context);
                PlatformMethods.INSTANCE.sendFeedback(context.getSource(), Utils.translatable("uniconfig.brigadier.collection.remove", broadcastName, entryWrapper.serialize(entry)), true);
            }
        });
        parent.then(removeArg);
        // clear
        ArgumentBuilder<SharedSuggestionProvider, ?> clearArg = LiteralArgumentBuilder.literal("clear");
        clearArg.executes(context -> {
            V collection = reader.apply(context);
            collection.clear();
            writer.accept(collection, context);
            PlatformMethods.INSTANCE.sendFeedback(context.getSource(), Utils.translatable("uniconfig.brigadier.collection.clear", broadcastName), true);
            return Command.SINGLE_SUCCESS;
        });
        writeArgModifier.accept(clearArg);
        parent.then(clearArg);
    }

    @Override
    public void readBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?> readArg, Function<CommandContext<SharedSuggestionProvider>, V> reader) {
        readArg.executes(context -> {
            PlatformMethods.INSTANCE.sendFeedback(context.getSource(), serializer.apply(reader.apply(context)), false);
            return Command.SINGLE_SUCCESS;
        });
    }

    @Override
    public void readWriteBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?> readArg, Consumer<ArgumentBuilder<SharedSuggestionProvider, ?>> writeArgModifier, Consumer<V> writer, Supplier<V> reader) {
        writeBuilder(broadcastName, readArg, writeArgModifier, c -> reader.get(), (a, b) -> writer.accept(a));
        readBuilder(broadcastName, readArg, a -> reader.get());
    }

}
