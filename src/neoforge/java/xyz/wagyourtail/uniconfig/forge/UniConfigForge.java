package xyz.wagyourtail.uniconfig.forge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("uniconfig")
public class UniConfigForge {
    private static final Logger LOGGER = LoggerFactory.getLogger(UniConfigForge.class);


    public UniConfigForge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onInitialize);
        NeoForge.EVENT_BUS.register(this);
    }

    public void onInitialize(FMLCommonSetupEvent event) {
        LOGGER.info("UniConfig initialized!");
    }

}
