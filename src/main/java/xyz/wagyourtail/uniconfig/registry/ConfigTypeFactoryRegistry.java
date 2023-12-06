package xyz.wagyourtail.uniconfig.registry;

import com.electronwill.nightconfig.core.Config;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ConfigTypeFactoryRegistry {
    private static final BiMap<ResourceLocation, ConfigType<?>> backing = Maps.synchronizedBiMap(HashBiMap.create());

    public static final ConfigType<Object> DEFAULT = register("unimined:default", ConfigType.of(Config::set, Config::get));


    public static <T> ConfigType<T> register(String id, ConfigType<T> serializer) {
        return register(new ResourceLocation(id), serializer);
    }

    public static <T> ConfigType<T> register(ResourceLocation id, ConfigType<T> serializer) {
        backing.put(id, serializer);
        return serializer;
    }

    public static <T> ConfigType<T> get(ResourceLocation id) {
        return (ConfigType<T>) backing.getOrDefault(id, DEFAULT);
    }

    public static <T> ResourceLocation getId(ConfigType<T> configType) {
        return backing.inverse().getOrDefault(configType, new ResourceLocation("unimined:default"));
    }

    public interface ConfigType<T> extends ConfigWriter<T>, ConfigReader<T> {


        static <T> ConfigType<T> of(ConfigWriter<T> writer, ConfigReader<T> reader) {
            return new ConfigType<>() {
                @Override
                public T read(Config config, List<String> key) {
                    return reader.read(config, key);
                }

                @Override
                public void write(Config config, List<String> key, T value) {
                    writer.write(config, key, value);
                }
            };
        }
    }

    @FunctionalInterface
    public interface ConfigWriter<T> {
        void write(Config config, List<String> key, T value);
    }

    @FunctionalInterface
    public interface ConfigReader<T> {
        T read(Config config, List<String> key);
    }

}
