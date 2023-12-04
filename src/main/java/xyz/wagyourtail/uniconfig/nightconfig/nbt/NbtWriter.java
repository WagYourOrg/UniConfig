package xyz.wagyourtail.uniconfig.nightconfig.nbt;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.WritingException;
import net.minecraft.nbt.*;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

public abstract class NbtWriter implements ConfigWriter {

    public abstract void write(CompoundTag tag, OutputStream stream) throws IOException;

    @Override
    public void write(UnmodifiableConfig config, Writer writer) {
        try (WriterOutputStream stream = WriterOutputStream.builder().setWriter(writer).get()) {
            CompoundTag tag = toTag(config);
            write(tag, stream);
        } catch (IOException e) {
            throw new WritingException(e);
        }
    }

    public CompoundTag toTag(UnmodifiableConfig config) {
        CompoundTag tag = new CompoundTag();
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            Object value = entry.getValue();
            tag.put(entry.getKey(), toTag(value));
        }
        return tag;
    }

    public Tag toTag(Object value) {
        if (value instanceof UnmodifiableConfig) {
            toTag((UnmodifiableConfig) value);
        } else if (value instanceof List<?>) {
            List<?> l = (List<?>) value;
            // check if all types in list are the same and a number type
            if (l.stream().allMatch(o -> o instanceof Number)) {
                if (l.stream().allMatch(o -> o instanceof Integer)) {
                    // int array
                    return new IntArrayTag((List<Integer>) l);
                } else if (l.stream().allMatch(o -> o instanceof Long)) {
                    // long array
                    return new net.minecraft.nbt.LongArrayTag((List<Long>) l);
                } else if (l.stream().allMatch(o -> o instanceof Byte)) {
                    // byte array
                    return new net.minecraft.nbt.ByteArrayTag((List<Byte>) l);
                }
            }
            // list
            ListTag lt = new ListTag();
            for (Object o : l) {
                lt.add(toTag(o));
            }
            return lt;
        } else if (value instanceof Number) {
            if (value instanceof Byte) {
                return ByteTag.valueOf((Byte) value);
            } else if (value instanceof Short) {
                return ShortTag.valueOf((Short) value);
            } else if (value instanceof Integer) {
                return IntTag.valueOf((Integer) value);
            } else if (value instanceof Long) {
                return LongTag.valueOf((Long) value);
            } else if (value instanceof Float) {
                return FloatTag.valueOf((Float) value);
            } else if (value instanceof Double) {
                return DoubleTag.valueOf((Double) value);
            }
        } else if (value instanceof Boolean) {
            throw new IllegalArgumentException("Boolean not supported");
        } else if (value instanceof String) {
            return StringTag.valueOf((String) value);
        }
        throw new IllegalArgumentException("Unknown type: " + value.getClass().getName());
    }
}
