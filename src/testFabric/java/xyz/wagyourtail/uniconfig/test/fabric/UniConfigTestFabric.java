package xyz.wagyourtail.uniconfig.test.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import xyz.wagyourtail.uniconfig.connector.brigadier.BrigadierConnector;
import xyz.wagyourtail.uniconfig.connector.screen.impl.ScrollScreen;
import xyz.wagyourtail.uniconfig.test.DeathSwapConfig;

public class UniConfigTestFabric implements ModInitializer {

    public static final DeathSwapConfig config = new DeathSwapConfig();

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register((d, b, s) -> {
            LiteralArgumentBuilder<CommandSourceStack> deathSwap = Commands.literal("deathswap");
            BrigadierConnector.register(config, deathSwap, r -> {}, w -> {});
            d.register(deathSwap);
        });

    }

}
