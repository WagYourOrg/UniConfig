package xyz.wagyourtail.uniconfig.nightconfig.nbt;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.*;
import java.util.Map;
import java.util.function.Supplier;

public abstract class NbtFormat implements ConfigFormat<Config> {
    public static NbtFormat COMPRESSED = new NbtFormat() {
        @Override
        public ConfigWriter createWriter() {
            return new NbtWriter() {
                @Override
                public void write(CompoundTag tag, OutputStream stream) throws IOException {
                    NbtIo.writeCompressed(tag, stream);
                }
            };
        }

        @Override
        public ConfigParser<Config> createParser() {
            return new NbtParser(this) {
                @Override
                CompoundTag read(InputStream reader) throws IOException {
                    return NbtIo.readCompressed(reader, NbtAccounter.unlimitedHeap());
                }
            };
        }
    };

    public static NbtFormat NORMAL = new NbtFormat() {
        @Override
        public ConfigWriter createWriter() {
            return new NbtWriter() {
                @Override
                public void write(CompoundTag tag, OutputStream stream) throws IOException {
                    NbtIo.write(tag, new DataOutputStream(stream));
                }
            };
        }

        @Override
        public ConfigParser<Config> createParser() {
            return new NbtParser(this) {
                @Override
                CompoundTag read(InputStream reader) throws IOException {
                    return NbtIo.read(new DataInputStream(reader));
                }
            };
        }
    };

    @Override
    public abstract ConfigWriter createWriter();

    @Override
    public abstract ConfigParser<Config> createParser();

    @Override
    public Config createConfig(Supplier<Map<String, Object>> mapCreator) {
        return Config.of(mapCreator, this).checked();
    }

    @Override
    public boolean supportsComments() {
        return false;
    }
}
