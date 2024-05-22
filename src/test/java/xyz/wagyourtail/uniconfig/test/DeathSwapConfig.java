package xyz.wagyourtail.uniconfig.test;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import xyz.wagyourtail.uniconfig.Group;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.UniConfig;
import xyz.wagyourtail.uniconfig.connector.brigadier.BrigadierConnector;
import xyz.wagyourtail.uniconfig.connector.brigadier.BrigadierSettingConnector;
import xyz.wagyourtail.uniconfig.connector.screen.ScreenConnector;
import xyz.wagyourtail.uniconfig.registry.ComponentFactoryRegistry;
import xyz.wagyourtail.uniconfig.registry.ConfigTypeFactoryRegistry;

import java.util.*;

public class DeathSwapConfig extends UniConfig {
    public static ConfigTypeFactoryRegistry.ConfigType<DeathSwapGameMode> DEATH_SWAP_GAME_MODE = ConfigTypeFactoryRegistry.register("deathswap:gamemode", ConfigTypeFactoryRegistry.ConfigType.ofEnum(DeathSwapGameMode.class));

    public DeathSwapConfig() {
        super("mod.deathswap.config", PlatformTestMethods.INSTANCE.getConfigPath().resolve("deathswap.toml"), false);
        this.requiredConnectors.add(BrigadierSettingConnector.class);
    }

    public final Group swapTime = group("swap_time");

    public final Setting<Integer> swapTimeMin = swapTime.setting("min", ComponentFactoryRegistry.TICK_TO_TIME, 20 * 60, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(TimeArgument.time(), ComponentFactoryRegistry.TICK_TO_TIME));
        ScreenConnector.intSlider(s, 0, 20 * 60);
    });

    public final Setting<Integer> swapTimeMax = swapTime.setting("max", ComponentFactoryRegistry.TICK_TO_TIME, 20 * 180, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(TimeArgument.time(), ComponentFactoryRegistry.TICK_TO_TIME));
        ScreenConnector.intSlider(s, 0, 20 * 180);
    });

    public final Setting<Integer> warnTime = swapTime.setting("warn_time", ComponentFactoryRegistry.TICK_TO_TIME, 0, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(TimeArgument.time(), ComponentFactoryRegistry.TICK_TO_TIME));
        ScreenConnector.intSlider(s, 0, 20 * 60);
    });

    public final Group spreadDistance = group("spread_distance");

    public final Setting<Integer> spreadDistanceMin = spreadDistance.setting("min", 10_000, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(IntegerArgumentType.integer(0), ComponentFactoryRegistry.TICK_TO_TIME));
        ScreenConnector.intSlider(s, 0, 30_000);
    });

    public final Setting<Integer> spreadDistanceMax = spreadDistance.setting("max", 20_000, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(IntegerArgumentType.integer(0), ComponentFactoryRegistry.TICK_TO_TIME));
        ScreenConnector.intSlider(s, 0, 50_000);
    });

    public final Setting<ResourceLocation> dimension = setting("dimension", Level.OVERWORLD.location(), ConfigTypeFactoryRegistry.RESOURCE_LOCATION, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(DimensionArgument.dimension()));
    });

    public final Setting<Integer> resistanceTime = setting("resistance_time", ComponentFactoryRegistry.TICK_TO_TIME, 20 * 15, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(TimeArgument.time(), ComponentFactoryRegistry.TICK_TO_TIME));
        ScreenConnector.intSlider(s, 0, 20 * 60);
    });

    public final Setting<Integer> maxStartFindTime = setting("max_start_find_time", ComponentFactoryRegistry.TICK_TO_TIME, 20 * 60 * 5, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(TimeArgument.time(), ComponentFactoryRegistry.TICK_TO_TIME));
        ScreenConnector.intSlider(s, 0, 20 * 60 * 10);
    });

    public final Group swapOptions = group("swap_options");

    public final Setting<Boolean> swapVelocity = swapOptions.setting("velocity", true, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
        ScreenConnector.bool(s);
    });

    public final Setting<Boolean> swapMount = swapOptions.setting("mount", true, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
        ScreenConnector.bool(s);
    });

    public final Setting<Boolean> swapHealth = swapOptions.setting("health", false, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
        ScreenConnector.bool(s);
    });

    public final Setting<Boolean> swapMobAgro = swapOptions.setting("mob_agro", true, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
        ScreenConnector.bool(s);
    });

    public final Setting<Boolean> swapHunger = swapOptions.setting("hunger", false, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
        ScreenConnector.bool(s);
    });

    public final Setting<Boolean> swapFire = swapOptions.setting("fire", false, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
        ScreenConnector.bool(s);
        s.onSettingUpdate(v -> {
            Minecraft.getInstance().gui.getChat().addMessage(Component.literal("fire: " + v));
        });
    });

    public final Setting<Boolean> swapAir = swapOptions.setting("air", false, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
        ScreenConnector.bool(s);
    });

    public final Setting<Boolean> swapFrozen = swapOptions.setting("frozen", false, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
        ScreenConnector.bool(s);
    });

    public final Setting<Boolean> swapPotionEffects = swapOptions.setting("potion_effects", false, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
        ScreenConnector.bool(s);
    });

    public final Setting<Boolean> swapInventory = swapOptions.setting("inventory", false, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
        ScreenConnector.bool(s);
    });

    public final Setting<Integer> teleportLoadTime = setting("teleport_load_time", ComponentFactoryRegistry.TICK_TO_TIME, 20 * 5, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(TimeArgument.time(), ComponentFactoryRegistry.TICK_TO_TIME));
        ScreenConnector.intSlider(s, 0, 20 * 60);
    });

    public final Setting<Boolean> debug = setting("debug", false, BrigadierConnector::empty);

    public final Setting<DeathSwapGameMode> gameMode = setting("game_mode", DeathSwapGameMode.NORMAL, DEATH_SWAP_GAME_MODE, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.enumValue(DeathSwapGameMode.class));
        ScreenConnector.enumCycle(s);
    });

    public final Group itemCount = group("item_count", g -> {
        ScreenConnector.group(g, gameMode, () -> gameMode.getValue() == DeathSwapGameMode.ITEM_COUNT);
    });

    public final Setting<Boolean> craftingCountsTowardsItemCount = itemCount.setting("crafting_counts_towards_item_count", true, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
        ScreenConnector.bool(s);
    });

    public final Setting<Map<String, String>> gameProperties = mapSetting("game_properties", new HashMap<>(), s -> {
        BrigadierConnector.connect(s, BrigadierConnector.map(
            BrigadierConnector.value(StringArgumentType.string()),
            BrigadierConnector.value(StringArgumentType.string()),
            s.textValue
        ));
    });

    public final Setting<List<String>> testList = listSetting("test_list", new ArrayList<>(), s -> {
        BrigadierConnector.connect(s, BrigadierConnector.collection(
                BrigadierConnector.value(StringArgumentType.string()),
                s.textValue
        ));
    });

}
