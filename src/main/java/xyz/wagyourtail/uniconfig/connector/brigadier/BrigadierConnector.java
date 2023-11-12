package xyz.wagyourtail.uniconfig.connector.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.ConfigSetting;
import xyz.wagyourtail.uniconfig.AbstractConnector;
import xyz.wagyourtail.uniconfig.UniConfig;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * connector to create a brigadier argument for a config item.
 * @param <T> type of config item value
 * @param <B> type of brigadier argument
 */
public class BrigadierConnector<T, B> extends AbstractConnector<T> {

    public final ArgumentType<B> brigadierType;
    public final Function<B, T> brigadierDeserializer;
    public final @Nullable BiFunction<CommandContext<SharedSuggestionProvider>, B, Boolean> brigadierFilter;
    public final @Nullable BrigadierSuggestor<SharedSuggestionProvider> brigadierSuggestor;

    protected BrigadierConnector(ConfigSetting<T, ?> item, ArgumentType<B> brigadierType, Function<B, T> brigadierDeserializer, @Nullable BiFunction<CommandContext<SharedSuggestionProvider>, B, Boolean> brigadierFilter, @Nullable BrigadierSuggestor<SharedSuggestionProvider> brigadierSuggestor) {
        super(item);
        this.brigadierType = brigadierType;
        this.brigadierDeserializer = brigadierDeserializer;
        this.brigadierFilter = brigadierFilter;
        this.brigadierSuggestor = brigadierSuggestor;
    }

    /**
     * use this one if you're explicitly requiring brigadier connectors, but don't want this setting to show up in the command.
     * @param item
     * @return
     * @param <T>
     */
    public static <T> BrigadierConnector<T, ?> empty(ConfigSetting<T, ?> item) {
        return create(item, null, null);
    }

    public static <T> BrigadierConnector<T, T> create(ConfigSetting<T, ?> item, ArgumentType<T> brigadierType) {
        return create(item, brigadierType, Function.identity());
    }

    public static <T, B> BrigadierConnector<T, B> create(ConfigSetting<T, ?> item, ArgumentType<B> brigadierType, Function<B, T> brigadierDeserializer) {
        return create(item, brigadierType, brigadierDeserializer, null);
    }

    public static <T, B> BrigadierConnector<T, B> create(ConfigSetting<T, ?> item, ArgumentType<B> brigadierType, Function<B, T> brigadierDeserializer, @Nullable BiFunction<CommandContext<SharedSuggestionProvider>, B, Boolean> brigadierFilter) {
        return create(item, brigadierType, brigadierDeserializer, brigadierFilter, null);
    }

    /**
     * @param item containing config item.
     * @param brigadierType brigadier argument type, used to create brigadier argument
     * @param brigadierDeserializer brigadier argument deserializer, used to convert brigadier argument to config item value
     * @param brigadierFilter brigadier argument filter, used for validation
     * @param brigadierSuggestor brigadier argument suggestor, used for tab completion
     */
    public static <T, B> BrigadierConnector<T, B> create(ConfigSetting<T, ?> item, ArgumentType<B> brigadierType, Function<B, T> brigadierDeserializer, @Nullable BiFunction<CommandContext<SharedSuggestionProvider>, B, Boolean> brigadierFilter, @Nullable BrigadierSuggestor<SharedSuggestionProvider> brigadierSuggestor) {
        return new BrigadierConnector<>(item, brigadierType, brigadierDeserializer, brigadierFilter, brigadierSuggestor);
    }

    @Override
    public Class<BrigadierConnector<T, ?>> getConnectorClass() {
        return (Class) BrigadierConnector.class;
    }

    public ArgumentBuilder<CommandSourceStack, ?> brigadierArgument() {
        if (brigadierType == null) return null;
        LiteralArgumentBuilder<CommandSourceStack> key = Commands.literal(item.stringKey());
        RequiredArgumentBuilder<CommandSourceStack, ?> value = Commands.argument("value", brigadierType);
        if (brigadierSuggestor != null) {
            value.suggests((context, builder) -> brigadierSuggestor.apply((CommandContext) context, builder));
        }
        value.executes(context -> {
            B newVal = (B) context.getArgument("value", Object.class);
            if (brigadierFilter != null && !brigadierFilter.apply((CommandContext) context, newVal)) {
                context.getSource().sendFailure(Component.translatable("uniconfig.brigadier.invalid", newVal.toString(), Component.translatable(item.stringKey())));
            } else {
                item.setValue(brigadierDeserializer.apply(newVal));
                item.group.parentConfig().save(item);
                context.getSource().sendSuccess(item::toText, true);
            }
            return Command.SINGLE_SUCCESS;
        });
        key.executes(context -> {
            context.getSource().sendSuccess(item::toText, false);
            return Command.SINGLE_SUCCESS;
        });
        return key.then(value);
    }

    public static void buildArguments(UniConfig config, ArgumentBuilder<CommandSourceStack, ?> parent) {
        LiteralArgumentBuilder<CommandSourceStack> reset = Commands.literal("reset");
        for (Map.Entry<String, ConfigSetting<?, ?>> entry : config.flatItems().entrySet()) {
            ConfigSetting configItem = entry.getValue();
            reset.then(Commands.literal(configItem.stringKey()).executes(context -> {
                configItem.setValue(configItem.defaultValue.get());
                context.getSource().sendSuccess(configItem::toText, true);
                return Command.SINGLE_SUCCESS;
            }));
            if (configItem.connectors.containsKey(BrigadierConnector.class)) {
                BrigadierConnector<?, ?> brigadierSerializer = (BrigadierConnector<?, ?>) configItem.connectors.get(BrigadierConnector.class);
                ArgumentBuilder<CommandSourceStack, ?> arg = brigadierSerializer.brigadierArgument();
                if (arg != null) {
                    parent.then(arg);
                }
                continue;
            }
            parent.executes(context -> {
                context.getSource().sendSuccess(config::toText, false);
                return Command.SINGLE_SUCCESS;
            });
        }
        reset.then(Commands.literal("all").executes(context -> {
            for (ConfigSetting item : config.flatItems().values()) {
                item.setValueNoSave(item.defaultValue.get());
            }
            config.save();
            context.getSource().sendSuccess(config::toText, true);
            return Command.SINGLE_SUCCESS;
        }));
        parent.then(reset);
    }

    @FunctionalInterface
    public interface BrigadierSuggestor<S> extends BiFunction<CommandContext<S>, SuggestionsBuilder, CompletableFuture<Suggestions>> {
    }

}
