package xyz.wagyourtail.uniconfig.connector.brigadier.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.connector.brigadier.BrigadierWrapper;
import xyz.wagyourtail.uniconfig.PlatformMethods;
import xyz.wagyourtail.uniconfig.util.Utils;

import java.util.function.*;

public class BrigadierValueWrapper<T, B> implements BrigadierWrapper<T, B> {
    public final ArgumentType<B> type;

    public final Function<B, T> deserializer;
    public final Function<T, Component> serializer;

    public final @Nullable BiPredicate<CommandContext<SharedSuggestionProvider>, B> filter;
    public final @Nullable SuggestionProvider<SharedSuggestionProvider> suggestor;

    private String writeKey;

    @ApiStatus.Internal
    public BrigadierValueWrapper(ArgumentType<B> type, String writeKey, Function<B, T> deserializer, Function<T, Component> serializer, @Nullable BiPredicate<CommandContext<SharedSuggestionProvider>, B> filter, @Nullable SuggestionProvider<SharedSuggestionProvider> suggestor) {
        this.type = type;
        this.writeKey = writeKey;
        this.deserializer = deserializer;
        this.serializer = serializer;
        this.filter = filter;
        this.suggestor = suggestor;
    }

    @Override
    public ArgumentType<B> getType() {
        return type;
    }

    @Override
    public String writeKey() {
        return writeKey;
    }

    @Override
    public void setWriteKey(String writeKey) {
        this.writeKey = writeKey;
    }

    @Override
    public T parse(Component broadcastName, String keyVal, CommandContext<SharedSuggestionProvider> context) {
        B value = (B) context.getArgument(writeKey(), Object.class);
        if (filter != null && !filter.test(context, value)) {
            PlatformMethods.INSTANCE.sendFailure(context.getSource(), Utils.translatable("uniconfig.brigadier.invalid", value.toString(), Utils.translatable("uniconfig.brigadier.map." + keyVal), broadcastName));
            return null;
        } else {
            return deserializer.apply(value);
        }
    }

    @Override
    public Component serialize(T value) {
        return serializer.apply(value);
    }

    @Override
    public void writeBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?>  parent, Consumer<ArgumentBuilder<SharedSuggestionProvider, ?>> writeArgModifier, Function<CommandContext<SharedSuggestionProvider>, T> reader, BiConsumer<T, CommandContext<SharedSuggestionProvider>> writer) {
        RequiredArgumentBuilder<SharedSuggestionProvider, ?> writeArg = RequiredArgumentBuilder.argument(writeKey(), type);
        writeArg.executes(context -> {
            T value = parse(broadcastName, "value", context);
            if (value != null) {
                writer.accept(value, context);
            }
            return Command.SINGLE_SUCCESS;
        });
        if (suggestor != null) {
            writeArg.suggests(suggestor);
        }
        writeArgModifier.accept(writeArg);
        parent.then(writeArg);
    }

    @Override
    public void readBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?> readArg, Function<CommandContext<SharedSuggestionProvider>, T> reader) {
        readArg.executes(context -> {
            PlatformMethods.INSTANCE.sendFeedback(context.getSource(), serializer.apply(reader.apply(context)), false);
            return Command.SINGLE_SUCCESS;
        });
    }

    @Override
    public void readWriteBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?> readArg, Consumer<ArgumentBuilder<SharedSuggestionProvider, ?>> writeArgModifier, Consumer<T> writer, Supplier<T> reader) {
        readBuilder(broadcastName, readArg, a -> reader.get());
        writeBuilder(broadcastName, readArg, writeArgModifier, c -> reader.get(), (a, b) -> {
            writer.accept(a);
            PlatformMethods.INSTANCE.sendFeedback(b.getSource(), Utils.translatable("uniconfig.brigadier.set", broadcastName, serializer.apply(a)), true);
        });
    }
}
