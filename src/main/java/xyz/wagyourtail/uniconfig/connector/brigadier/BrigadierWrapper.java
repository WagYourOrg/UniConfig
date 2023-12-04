package xyz.wagyourtail.uniconfig.connector.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface BrigadierWrapper<T, B> {

    ArgumentType<B> getType();

    String writeKey();

    void setWriteKey(String writeKey);

    @Nullable
    T parse(Component broadcastName, String keyVal, CommandContext<SharedSuggestionProvider> context);

    Component serialize(T value);

    void writeBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?>  parent, Consumer<ArgumentBuilder<SharedSuggestionProvider, ?>> writeArgModifier, Function<CommandContext<SharedSuggestionProvider>, T> reader, BiConsumer<T, CommandContext<SharedSuggestionProvider>> writer);

    void readBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?> readArg, Function<CommandContext<SharedSuggestionProvider>, T> reader);

    void readWriteBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?> readArg, Consumer<ArgumentBuilder<SharedSuggestionProvider, ?>> writeArgModifier, Consumer<T> writer, Supplier<T> reader);

}
