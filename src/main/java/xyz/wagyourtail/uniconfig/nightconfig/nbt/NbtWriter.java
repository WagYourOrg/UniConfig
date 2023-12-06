package xyz.wagyourtail.uniconfig.nightconfig.nbt;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.WritingException;
import net.minecraft.nbt.*;
import org.apache.commons.io.output.WriterOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
        } else if (value instanceof Collection<?> l) {
            if (l.isEmpty()) {
                return new ListTag();
            }
            // check if all types in list are the same and a number type
            if (l.stream().allMatch(o -> o instanceof Number)) {
                if (l.stream().allMatch(o -> o instanceof Integer)) {
                    // int array
                    return new IntArrayTag((List) List.copyOf(l));
                } else if (l.stream().allMatch(o -> o instanceof Long)) {
                    // long array
                    return new LongArrayTag((List) List.copyOf(l));
                } else if (l.stream().allMatch(o -> o instanceof Boolean)) {
                    // byte array
                    return new ByteArrayTag(l.stream().map(e -> (byte) ((boolean) e ? 1 : 0)).collect(Collectors.toList()));
                }
            }
            // detect if homogenous list
            Class<?> clazz = l.iterator().next().getClass();
            if (l.stream().allMatch(o -> o.getClass().equals(clazz))) {
                // homogenous list
                ListTag lt = new ListTag();
                for (Object o : l) {
                    lt.add(toTag(o));
                }
                return lt;
            } else {
                // heterogeneous list
                ListTag lt = new ListTag();
                for (Object o : l) {
                    CompoundTag tag = new CompoundTag();
                    tag.put("", toTag(o));
                    lt.add(tag);
                }
                return lt;
            }
        } else if (value instanceof Number) {
            if (value instanceof Integer) {
                return IntTag.valueOf((Integer) value);
            } else if (value instanceof Long) {
                return LongTag.valueOf((Long) value);
            } else if (value instanceof Float) {
                return FloatTag.valueOf((Float) value);
            } else if (value instanceof Double) {
                return DoubleTag.valueOf((Double) value);
            }
        } else if (value instanceof Boolean) {
            return ByteTag.valueOf((Boolean) value ? (byte) 1 : (byte) 0);
        } else if (value instanceof String) {
            return StringTag.valueOf((String) value);
        }
        throw new IllegalArgumentException("Unknown type: " + value.getClass().getName());
    }
}
