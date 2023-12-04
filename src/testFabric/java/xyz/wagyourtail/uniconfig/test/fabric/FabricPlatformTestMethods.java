package xyz.wagyourtail.uniconfig.test.fabric;

import net.fabricmc.loader.api.FabricLoader;
import xyz.wagyourtail.uniconfig.test.PlatformTestMethods;

import java.nio.file.Path;

public class FabricPlatformTestMethods implements PlatformTestMethods {

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

}
