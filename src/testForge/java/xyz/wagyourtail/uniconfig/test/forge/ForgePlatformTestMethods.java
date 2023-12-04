package xyz.wagyourtail.uniconfig.test.forge;

import net.neoforged.fml.loading.FMLPaths;
import xyz.wagyourtail.uniconfig.test.PlatformTestMethods;

import java.nio.file.Path;

public class ForgePlatformTestMethods implements PlatformTestMethods {

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

}
