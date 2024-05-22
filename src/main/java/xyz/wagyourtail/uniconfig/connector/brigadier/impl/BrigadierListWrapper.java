package xyz.wagyourtail.uniconfig.connector.brigadier.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import xyz.wagyourtail.uniconfig.connector.brigadier.BrigadierWrapper;
import xyz.wagyourtail.uniconfig.PlatformMethods;
import xyz.wagyourtail.uniconfig.util.TranslationUtils;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class BrigadierListWrapper<T, B> extends BrigadierCollectionWrapper<T, B, List<T>> {

    public BrigadierListWrapper(BrigadierWrapper<T, B> entryWrapper, Function<List<T>, Component> serializer) {
        super(entryWrapper, serializer);
    }

    @Override
    public void writeBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?> parent, Consumer<ArgumentBuilder<SharedSuggestionProvider, ?>> writeArgModifier, Function<CommandContext<SharedSuggestionProvider>, List<T>> reader, BiConsumer<List<T>, CommandContext<SharedSuggestionProvider>> writer) {
        super.writeBuilder(broadcastName, parent, writeArgModifier, reader, writer);
        // insert
        ArgumentBuilder<SharedSuggestionProvider, ?> insertArg = LiteralArgumentBuilder.literal("insert");
        ArgumentBuilder<SharedSuggestionProvider, ?> indexArg = insertArg.then(RequiredArgumentBuilder.argument("index", IntegerArgumentType.integer(0)));
        entryWrapper.writeBuilder(broadcastName, indexArg, writeArgModifier, c -> {
            throw new UnsupportedOperationException();
        }, (value, context) -> {
            List<T> collection = reader.apply(context);
            int index = IntegerArgumentType.getInteger(context, "index");
            T entry = entryWrapper.parse(broadcastName, "value", context);
            if (entry != null) {
                collection.add(index, entry);
                writer.accept(collection, context);
                PlatformMethods.INSTANCE.sendFeedback(context.getSource(), TranslationUtils.translatable("uniconfig.brigadier.list.inserted", broadcastName, entryWrapper.serialize(entry), index), true);
            }
        });
        parent.then(insertArg.then(indexArg));
    }

    @Override
    public void readBuilder(Component broadcastName, ArgumentBuilder<SharedSuggestionProvider, ?> readArg, Function<CommandContext<SharedSuggestionProvider>, List<T>> reader) {
        super.readBuilder(broadcastName, readArg, reader);
        // get
        ArgumentBuilder<SharedSuggestionProvider, ?> getArg = LiteralArgumentBuilder.literal("get");
        ArgumentBuilder<SharedSuggestionProvider, ?> indexArg = getArg.then(RequiredArgumentBuilder.argument("index", IntegerArgumentType.integer(0)));
        entryWrapper.readBuilder(broadcastName, indexArg, c -> {
            int index = IntegerArgumentType.getInteger(c, "index");
            List<T> collection = reader.apply(c);
            if (index >= 0 && index < collection.size()) {
                return collection.get(index);
            } else {
                PlatformMethods.INSTANCE.sendFailure(c.getSource(), TranslationUtils.translatable("uniconfig.brigadier.list.invalid_index", index, collection.size()));
                return null;
            }
        });
        readArg.then(getArg.then(indexArg));
    }
}
