package xyz.wagyourtail.uniconfig.connector.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.ConfigSetting;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BrigadierMapConnector<T, U, BK, BV> extends BrigadierConnector<Map<T, U>, BK> {


    protected BrigadierMapConnector(ConfigSetting<Map<T, U>, ?> item, ArgumentType<BK> brigadierType, Function<BK, Map<T, U>> brigadierDeserializer, @Nullable BiFunction<CommandContext<SharedSuggestionProvider>, BK, Boolean> brigadierFilter, @Nullable BrigadierSuggestor<SharedSuggestionProvider> brigadierSuggestor) {
        super(item, brigadierType, brigadierDeserializer, brigadierFilter, brigadierSuggestor);
    }




    // put, get, remove, clear

}
