package xyz.wagyourtail.uniconfig.test;

import java.nio.file.Path;
import java.util.ServiceLoader;

public interface PlatformTestMethods {
    PlatformTestMethods INSTANCE = ServiceLoader.load(PlatformTestMethods.class).iterator().next();

    Path getConfigPath();

}
