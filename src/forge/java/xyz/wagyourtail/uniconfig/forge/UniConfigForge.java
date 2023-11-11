package xyz.wagyourtail.uniconfig.forge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("uniconfig")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class UniConfigForge {
    private static final Logger LOGGER = LoggerFactory.getLogger(UniConfigForge.class);

    @SubscribeEvent
    public static void onInitialize(FMLCommonSetupEvent event) {
        LOGGER.info("UniConfig initialized!");
    }

}
