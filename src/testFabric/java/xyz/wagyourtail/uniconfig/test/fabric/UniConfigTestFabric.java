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
import xyz.wagyourtail.uniconfig.connector.brigadier.Brigadier;
import xyz.wagyourtail.uniconfig.connector.screen.impl.ScrollScreen;
import xyz.wagyourtail.uniconfig.test.DeathSwapConfig;

public class UniConfigTestFabric implements ModInitializer, ClientModInitializer {

    public static final DeathSwapConfig config = new DeathSwapConfig();

    @Environment(EnvType.CLIENT)
    public static final KeyMapping DEATH_SWAP_KEY = new KeyMapping("deathswap.config", InputConstants.Type.KEYSYM, InputConstants.KEY_J, "key.categories.misc");

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register((d, b, s) -> {
            LiteralArgumentBuilder<CommandSourceStack> deathSwap = Commands.literal("deathswap");
            Brigadier.register(config, deathSwap, r -> {}, w -> {});
            d.register(deathSwap);
        });

    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(DEATH_SWAP_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (DEATH_SWAP_KEY.consumeClick()) {
                client.setScreen(new ScrollScreen(client.screen, config));
            }
        });
    }

}
