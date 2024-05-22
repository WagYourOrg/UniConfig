package xyz.wagyourtail.uniconfig.connector.network;

import com.electronwill.nightconfig.core.Config;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.uniconfig.Group;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.UniConfig;
import xyz.wagyourtail.uniconfig.nightconfig.nbt.NbtFormat;
import xyz.wagyourtail.uniconfig.nightconfig.nbt.NbtParser;
import xyz.wagyourtail.uniconfig.nightconfig.nbt.NbtWriter;
import xyz.wagyourtail.uniconfig.registry.ComponentFactoryRegistry;
import xyz.wagyourtail.uniconfig.registry.ConfigTypeFactoryRegistry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Experimental
public class NetworkConfig extends UniConfig {

    public NetworkConfig(@NotNull String name, Consumer<FriendlyByteBuf> sendToServer) {
        super(
            name,
            NbtFormat.COMPRESSED.createConfig(),
            () -> new ByteBufOutputStream(new FriendlyByteBuf(Unpooled.buffer())) {

                @Override
                public void close() throws IOException {
                    super.close();
                    sendToServer.accept((FriendlyByteBuf) this.buffer());
                }
            },
            true
        );
    }

    private static final NbtWriter writer = (NbtWriter) NbtFormat.COMPRESSED.createWriter();
    private static final NbtParser parser = (NbtParser) NbtFormat.COMPRESSED.createParser();

    private CompoundTag groupSerializer(Group group) {
        CompoundTag compound = new CompoundTag();
        CompoundTag groups = new CompoundTag();
        compound.put("groups", groups);
        for (Group child : group.children) {
            groups.put(child.name, groupSerializer(child));
        }
        CompoundTag settings = new CompoundTag();
        compound.put("settings", settings);
        for (Setting setting : group.configItems.values()) {
            settings.put(setting.name, settingSerializer(setting));
        }
        // TODO: connectors
        return compound;
    }

    private CompoundTag settingSerializer(Setting setting) {
        Config item = NbtFormat.COMPRESSED.createConfig();
        setting.serializer.write(item, List.of("value"), setting.getValue());
        setting.serializer.write(item, List.of("default"), setting.defaultValue.get());
        setting.serializer.write(item, List.of("textifier"), ComponentFactoryRegistry.getId(setting.textValue).orElseThrow().toString());
        setting.serializer.write(item, List.of("type"), ConfigTypeFactoryRegistry.getId(setting.serializer).orElseThrow().toString());
        // TODO: connectors
        return writer.toTag(item);
    }

    public CompoundTag sendToClient() {
        return groupSerializer(this);
    }

    public static NetworkConfig readFromServer(String name, CompoundTag tag, Consumer<FriendlyByteBuf> sendToServer) {
        NetworkConfig config = new NetworkConfig(name, sendToServer);
        readGroupFromServer(config, tag);
        return config;
    }

    public static void readGroupFromServer(Group target, CompoundTag tag) {
        for (String key : tag.getAllKeys()) {
            if (key.equals("groups")) {
                CompoundTag groups = tag.getCompound(key);
                for (String groupKey : groups.getAllKeys()) {
                    Group group = new Group(groupKey, target);
                    target.children.add(group);
                    readGroupFromServer(group, groups.getCompound(groupKey));
                }
            } else if (key.equals("settings")) {
                CompoundTag settings = tag.getCompound(key);
                for (String settingKey : settings.getAllKeys()) {
                    CompoundTag setting = settings.getCompound(settingKey);
                    ConfigTypeFactoryRegistry.ConfigType<?> type = ConfigTypeFactoryRegistry.get(new ResourceLocation(setting.getString("type"))).orElseThrow();
                    Function<?, Component> textifier = ComponentFactoryRegistry.get(new ResourceLocation(setting.getString("textifier"))).orElseThrow();
                    Setting settingObj = new Setting(
                        settingKey,
                        target,
                        textifier,
                        () -> parser.fromTag(Objects.requireNonNull(setting.get("default"))),
                        type
                    );
                    settingObj.setValue(parser.fromTag(Objects.requireNonNull(setting.get("value"))));
                    // TODO: connectors
                    target.setting(settingKey, settingObj);
                }
            } else {
                // TODO: connectors
                throw new IllegalStateException("Unknown key: " + key);
            }
        }
    }

}
