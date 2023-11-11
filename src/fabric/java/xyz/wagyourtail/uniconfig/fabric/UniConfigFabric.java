package xyz.wagyourtail.uniconfig.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniConfigFabric implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(UniConfigFabric.class);

    @Override
    public void onInitialize() {
        LOGGER.info("UniConfig initialized!");
    }

}
