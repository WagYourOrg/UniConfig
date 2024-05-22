package xyz.wagyourtail.uniconfig.registry;

import com.electronwill.nightconfig.core.Config;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ConfigTypeFactoryRegistry {
    private static final BiMap<ResourceLocation, ConfigType<?>> backing = Maps.synchronizedBiMap(HashBiMap.create());

    public static final ConfigType<Object> DEFAULT = register("uniconfig:default", ConfigType.of(Config::set, Config::get));
    public static final ConfigType<ResourceLocation> RESOURCE_LOCATION = register("uniconfig:resource_location", ConfigType.of(ConfigType.stringWriter(), ConfigType.stringReader(ResourceLocation::new)));

    public static <T> ConfigType<T> register(String id, ConfigType<T> serializer) {
        return register(new ResourceLocation(id), serializer);
    }

    public static <T> ConfigType<T> register(ResourceLocation id, ConfigType<T> serializer) {
        backing.put(id, serializer);
        return serializer;
    }

    public static <T> Optional<ConfigType<T>> get(ResourceLocation id) {
        return Optional.ofNullable((ConfigType<T>) backing.get(id));
    }

    public static <T> Optional<ResourceLocation> getId(ConfigType<T> configType) {
        return Optional.ofNullable(backing.inverse().get(configType));
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

        static <T extends Enum<T>> ConfigType<T> ofEnum(Class<T> enumClass) {
            return of(ConfigType.stringWriter(Enum::name), ConfigType.stringReader(s -> Enum.valueOf(enumClass, s)));
        }

        static <T> ConfigWriter<T> stringWriter() {
            return (config, key, value) -> config.set(key, value.toString());
        }

        static <T> ConfigWriter<T> stringWriter(Function<T, String> serializer) {
            return (config, key, value) -> config.set(key, serializer.apply(value));
        }

        static <T> ConfigReader<T> stringReader(Function<String, T> parser) {
            return (config, key) -> parser.apply(config.get(key));
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
