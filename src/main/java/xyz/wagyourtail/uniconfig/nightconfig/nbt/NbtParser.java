package xyz.wagyourtail.uniconfig.nightconfig.nbt;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import net.minecraft.nbt.*;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class NbtParser implements ConfigParser<Config> {

    private final NbtFormat format;

    public NbtParser(NbtFormat format) {
        this.format = format;
    }

    @Override
    public ConfigFormat<Config> getFormat() {
        return format;
    }

    abstract CompoundTag read(InputStream reader) throws IOException;

    @Override
    public Config parse(Reader reader) {
        Config config = format.createConfig();
        parse(reader, config, ParsingMode.MERGE);
        return config;
    }

    public void parse(List<String> keys, CompoundTag tag, Config config, ParsingMode parsingMode) {
        for (String key : tag.getAllKeys()) {
            Tag entry = tag.get(key);
            assert entry != null;
            keys.add(key);
            if (entry instanceof CompoundTag) {
                parse(keys, (CompoundTag) entry, config, parsingMode);
            } else {
                parsingMode.put(config, keys, fromTag(entry));
            }
            keys.remove(keys.size() - 1);
        }
    }

    public Object fromTag(Tag tag) {
        switch (tag.getId()) {
            case Tag.TAG_BYTE:
                return ((ByteTag) tag).getAsByte();
            case Tag.TAG_SHORT:
                return ((ShortTag) tag).getAsShort();
            case Tag.TAG_INT:
                return ((IntTag) tag).getAsInt();
            case Tag.TAG_LONG:
                return ((LongTag) tag).getAsLong();
            case Tag.TAG_FLOAT:
                return ((FloatTag) tag).getAsFloat();
            case Tag.TAG_DOUBLE:
                return ((DoubleTag) tag).getAsDouble();
            case Tag.TAG_BYTE_ARRAY: {
                List<Byte> l = new ArrayList<>();
                for (byte b : ((ByteArrayTag) tag).getAsByteArray()) {
                    l.add(b);
                }
                return l;
            }
            case Tag.TAG_STRING:
                return tag.getAsString();
            case Tag.TAG_LIST: {
                ListTag list = (ListTag) tag;
                List<Object> l = new ArrayList<>();
                for (Tag t : list) {
                    l.add(fromTag(t));
                }
                return l;
            }
            case Tag.TAG_INT_ARRAY: {
                List<Integer> l = new ArrayList<>();
                for (int i : ((IntArrayTag) tag).getAsIntArray()) {
                    l.add(i);
                }
                return l;
            }
            case Tag.TAG_LONG_ARRAY: {
                List<Long> l = new ArrayList<>();
                for (long i : ((LongArrayTag) tag).getAsLongArray()) {
                    l.add(i);
                }
                return l;
            }
            default:
                throw new ParsingException("unknown tag type: " + tag.getId());
        }
    }

    @Override
    public void parse(Reader reader, Config config, ParsingMode parsingMode) {
        parsingMode.prepareParsing(config);
        try (InputStream stream = ReaderInputStream.builder().setReader(reader).get()) {
            CompoundTag readConfig = read(stream);
            parse(new ArrayList<>(), readConfig, config, parsingMode);
        } catch (IOException e) {
            throw new ParsingException(e.getMessage());
        }
    }
}
