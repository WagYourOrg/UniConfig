package xyz.wagyourtail.uniconfig.connector.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.ConfigSetting;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BrigadierCollectionConnector<T, B> extends BrigadierConnector<Collection<T>, B> {
    public final BiFunction<B, Integer, Collection<T>> brigadierListInsertDeserializer;
    public final Function<B, Collection<T>> brigadierRemoveDeserializer;
    public final Function<T, Component> valueSerializer;

    protected BrigadierCollectionConnector(ConfigSetting<Collection<T>, ?> item, ArgumentType<B> brigadierType, Function<B, T> brigadierDeserializer, @Nullable BiFunction<CommandContext<SharedSuggestionProvider>, B, Boolean> brigadierFilter, @Nullable BrigadierSuggestor<SharedSuggestionProvider> brigadierSuggestor, Function<T, Component> valueSerializer) {
        super(item, brigadierType, (e) -> {
            item.getValue().add(brigadierDeserializer.apply(e));
            return item.getValue();
        }, brigadierFilter, brigadierSuggestor);
        if (item.getValue() instanceof List) {
            brigadierListInsertDeserializer = (e, i) -> {
                ((List) item.getValue()).add(i, brigadierDeserializer.apply(e));
                return item.getValue();
            };
        } else {
            brigadierListInsertDeserializer = null;
        }
        brigadierRemoveDeserializer = (e) -> {
            item.getValue().remove(brigadierDeserializer.apply(e));
            return item.getValue();
        };
        this.valueSerializer = valueSerializer;
    }

    // add, insert, get, remove, clear (insert/get only if list)
    public ArgumentBuilder<CommandSourceStack, ?> brigadierArgument() {
        if (brigadierType == null) return null;
        LiteralArgumentBuilder<CommandSourceStack> key = Commands.literal(item.stringKey());
        // add
        {
            LiteralArgumentBuilder<CommandSourceStack> add = Commands.literal("add");
            RequiredArgumentBuilder<CommandSourceStack, ?> valueArg = Commands.argument("value", brigadierType);
            if (brigadierSuggestor != null) {
                valueArg.suggests((context, builder) -> brigadierSuggestor.apply((CommandContext) context, builder));
            }
            add.then(valueArg.executes(context -> {
                B newVal = (B) context.getArgument("value", Object.class);
                if (!brigadierFilter.apply((CommandContext) context, newVal)) {
                    context.getSource().sendFailure(Component.translatable("uniconfig.brigadier.invalid", newVal.toString(), Component.translatable(item.stringKey())));
                } else {
                    item.setValue(brigadierDeserializer.apply(newVal));
                    item.group.parentConfig().save(item);
                    context.getSource().sendSuccess(item::toText, true);
                }
                return Command.SINGLE_SUCCESS;
            }));
            key.then(add);
        }
        if (brigadierListInsertDeserializer != null) {
            // insert
            {
                LiteralArgumentBuilder<CommandSourceStack> insert = Commands.literal("insert");
                RequiredArgumentBuilder<CommandSourceStack, ?> indexArg = Commands.argument("index", IntegerArgumentType.integer(0));
                RequiredArgumentBuilder<CommandSourceStack, ?> valueArg = Commands.argument("value", brigadierType);
                insert.then(indexArg.then(valueArg.executes(context -> {
                    B newVal = (B) context.getArgument("value", Object.class);
                    int index = context.getArgument("index", Integer.class);
                    if (!brigadierFilter.apply((CommandContext) context, newVal)) {
                        context.getSource().sendFailure(Component.translatable("uniconfig.brigadier.invalid", newVal.toString(), Component.translatable(item.stringKey())));
                    } else {
                        item.setValue(brigadierListInsertDeserializer.apply(newVal, index));
                        item.group.parentConfig().save(item);
                        context.getSource().sendSuccess(item::toText, true);
                    }
                    return Command.SINGLE_SUCCESS;
                })));
                key.then(insert);
            }
            // get
            {
                LiteralArgumentBuilder<CommandSourceStack> get = Commands.literal("get");
                RequiredArgumentBuilder<CommandSourceStack, ?> indexArg = Commands.argument("index", IntegerArgumentType.integer(0));
                get.then(indexArg.executes(context -> {
                    int index = context.getArgument("index", Integer.class);
                    Collection<T> list = item.getValue();
                    index = Math.max(0, Math.min(index, list.size() - 1));
                    T value = (T) ((List) list).get(index);
                    context.getSource().sendSuccess(() -> valueSerializer.apply(value), false);
                    return Command.SINGLE_SUCCESS;
                }));
            }
        }
        // remove
        {
            LiteralArgumentBuilder<CommandSourceStack> remove = Commands.literal("remove");
            RequiredArgumentBuilder<CommandSourceStack, ?> valueArg = Commands.argument("value", brigadierType);
            if (brigadierSuggestor != null) {
                valueArg.suggests((context, builder) -> brigadierSuggestor.apply((CommandContext) context, builder));
            }
            remove.then(valueArg.executes(context -> {
                B newVal = (B) context.getArgument("value", Object.class);
                if (!brigadierFilter.apply((CommandContext) context, newVal)) {
                    context.getSource().sendFailure(Component.translatable("uniconfig.brigadier.invalid", newVal.toString(), Component.translatable(item.stringKey())));
                } else {
                    item.setValue(brigadierRemoveDeserializer.apply(newVal));
                    context.getSource().sendSuccess(item::toText, true);
                }
                return Command.SINGLE_SUCCESS;
            }));
        }
        // clear
        {
            LiteralArgumentBuilder<CommandSourceStack> clear = Commands.literal("clear");
            clear.executes(context -> {
                item.setValueNoSave(item.defaultValue.get());
                item.group.parentConfig().save(item);
                context.getSource().sendSuccess(item::toText, true);
                return Command.SINGLE_SUCCESS;
            });
        }
        key.executes(context -> {
            context.getSource().sendSuccess(item::toText, false);
            return Command.SINGLE_SUCCESS;
        });
        return key;
    }



}
