package xyz.wagyourtail.uniconfig.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Function;

public class ComponentFactoryRegistry {
    private static final BiMap<ResourceLocation, Function<?, Component>> backing = Maps.synchronizedBiMap(HashBiMap.create());

    public static final Function<Object, Component> DEFAULT = register("uniconfig:default", o -> Component.literal(o.toString()));
    public static final Function<Integer, Component> TICK_TO_TIME = register("uniconfig:tick_to_time", i -> {
        StringBuilder sb = new StringBuilder();
        int ticks = i % 20;
        int seconds = (i / 20) % 60;
        int minutes = (i / (20 * 60)) % 60;
        int hours = (i / (20 * 60 * 60)) % 24;
        int days = (i / (20 * 60 * 60 * 24));
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || !sb.isEmpty()) sb.append(hours).append(":");
        if (minutes > 0 || !sb.isEmpty()) sb.append(String.format("%2d", minutes)).append(":");
        if (seconds > 0 || !sb.isEmpty()) sb.append(String.format("%2d", seconds)).append(i > 20 * 60 ? " " : "s ");
        if (ticks > 0 || i == 0) sb.append(ticks).append("t ");
        sb.setLength(sb.length() - 1);
        return Component.literal(sb.toString());
    });

    // todo: more translatable with units, ie "%d blocks"

    public static <T> Function<T, Component> register(String id, Function<T, Component> serializer) {
        backing.put(new ResourceLocation(id), serializer);
        return serializer;
    }

    public static <T> Function<T, Component> register(ResourceLocation id, Function<T, Component> serializer) {
        backing.put(id, serializer);
        return serializer;
    }

    public static <T> Optional<Function<T, Component>> get(ResourceLocation id) {
        return Optional.ofNullable((Function<T, Component>) backing.get(id));
    }

    public static <T> Optional<ResourceLocation> getId(Function<T, Component> serializer) {
        return Optional.ofNullable(backing.inverse().get(serializer));
    }

    private ComponentFactoryRegistry() {
    }

}
