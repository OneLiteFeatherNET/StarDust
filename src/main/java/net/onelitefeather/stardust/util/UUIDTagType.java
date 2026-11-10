package net.onelitefeather.stardust.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class UUIDTagType implements PersistentDataType<byte[], UUID> {

    public static final UUIDTagType UUID = new UUIDTagType();

    @Override
    public @NonNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NonNull Class<UUID> getComplexType() {
        return UUID.class;
    }

    @Override
    public byte @NonNull [] toPrimitive(UUID complex, @NonNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(complex.getMostSignificantBits());
        bb.putLong(complex.getLeastSignificantBits());
        return bb.array();
    }

    @Override
    public @NonNull UUID fromPrimitive(byte @NonNull [] primitive, @NonNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(primitive);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

}
