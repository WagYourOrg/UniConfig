package xyz.wagyourtail.uniconfig.connector.brigadier;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.uniconfig.SettingConnector;
import xyz.wagyourtail.uniconfig.Setting;

import java.util.function.*;

public class BrigadierConnector<T> extends SettingConnector<T> {
    public final boolean disabled;
    public final BrigadierWrapper<T, ?> wrapper;

    @SuppressWarnings("DataFlowIssue")
    public BrigadierConnector(Setting<T> setting) {
        super(setting);
        this.wrapper = null;
        this.disabled = true;
    }

    public BrigadierConnector(Setting<T> setting, BrigadierWrapper<T, ?> wrapper) {
        super(setting);
        this.wrapper = wrapper;
        this.disabled = false;
    }

    public @Nullable ArgumentBuilder<SharedSuggestionProvider, ?> createArgumentBuilder(Consumer<ArgumentBuilder<SharedSuggestionProvider, ?>> readArg, Consumer<ArgumentBuilder<SharedSuggestionProvider, ?>> writeArg) {
        if (disabled) return null;
        ArgumentBuilder<SharedSuggestionProvider, ?> arg = LiteralArgumentBuilder.literal(item.nameKey());
        readArg.accept(arg);
        wrapper.readWriteBuilder(
            item.name(),
            arg,
            writeArg,
            item::setValue,
            item::getValue
        );
        return arg;
    }

    @Override
    public Class<? extends SettingConnector<?>> getConnectorClass() {
        return (Class) BrigadierConnector.class;
    }

}
