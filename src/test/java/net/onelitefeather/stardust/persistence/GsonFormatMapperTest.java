package net.onelitefeather.stardust.persistence;

import com.google.gson.reflect.TypeToken;
import net.onelitefeather.stardust.api.player.setting.PlayerSetting;
import org.hibernate.type.descriptor.java.JavaType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GsonFormatMapperTest {

    private final GsonFormatMapper mapper = new GsonFormatMapper();
    private final Type mapType = new TypeToken<Map<String, PlayerSetting>>() { }.getType();

    @SuppressWarnings("unchecked")
    private JavaType<Map<String, PlayerSetting>> javaType() {
        return (JavaType<Map<String, PlayerSetting>>) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{JavaType.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getJavaType")) return mapType;
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @Test
    void writesDocumentedJsonShape() {
        Map<String, PlayerSetting> map = Map.of("flying",
                new PlayerSetting("true", 1, Instant.parse("2026-01-02T03:04:05Z")));

        String json = mapper.toString(map, javaType(), null);

        assertEquals("{\"flying\":{\"value\":\"true\",\"schema\":1,\"updatedAt\":\"2026-01-02T03:04:05Z\"}}", json);
    }

    @Test
    void roundTripsSettingsIncludingInstant() {
        Map<String, PlayerSetting> map = new LinkedHashMap<>();
        map.put("a", new PlayerSetting("x", 2, Instant.parse("2026-05-06T07:08:09.123Z")));
        map.put("b", new PlayerSetting("y", 1, Instant.parse("2020-01-01T00:00:00Z")));

        String json = mapper.toString(map, javaType(), null);
        Map<String, PlayerSetting> read = mapper.fromString(json, javaType(), null);

        assertEquals(map, read);
    }

    @Test
    void readsEmptyObjectAsEmptyMap() {
        assertTrue(mapper.fromString("{}", javaType(), null).isEmpty());
    }
}
