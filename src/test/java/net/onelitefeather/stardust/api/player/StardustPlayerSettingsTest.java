package net.onelitefeather.stardust.api.player;

import net.onelitefeather.stardust.api.player.setting.PlayerSetting;
import net.onelitefeather.stardust.api.player.setting.SettingKey;
import net.onelitefeather.stardust.api.player.setting.SettingValueCodec;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StardustPlayerSettingsTest {

    private static final Instant NOW = Instant.parse("2026-01-02T03:04:05Z");

    private final StardustPlayer player = new StardustPlayer(UUID.randomUUID(), UUID.randomUUID(), "Steve", new HashMap<>(), NOW, NOW);
    private final SettingKey<Boolean> flying = SettingKey.builder(Boolean.class, "flying")
            .defaultValue(false).schemaVersion(1).build();

    @Test
    void returnsDefaultWhenSettingIsAbsent() {
        assertEquals(false, player.getSetting(flying), "absent setting must yield the default");
    }

    @Test
    void storesNonDefaultValueWithSchemaAndClockTime() {
        player.setSetting(flying, true);

        PlayerSetting stored = player.getSettings().get("flying");
        assertNotNull(stored, "non-default value must be persisted");
        assertEquals(true, SettingValueCodec.decode(Boolean.class, stored.value()));
        assertEquals(1, stored.schema());
        assertNotEquals(NOW, stored.updatedAt());
        assertTrue(player.getSetting(flying));
    }

    @Test
    void appliesMigrationWhenStoredSchemaIsOlder() {
        SettingKey<String> v1 = SettingKey.builder(String.class, "mode").defaultValue("none").schemaVersion(1).build();
        SettingKey<String> v2 = SettingKey.builder(String.class, "mode")
                .defaultValue("none").schemaVersion(2).migration(String::toUpperCase).build();
        Clock earlier = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        StardustPlayer old = new StardustPlayer(UUID.randomUUID(), UUID.randomUUID(), "Alex", earlier.instant(), NOW);
        old.setSetting(v1, "fast");

        assertEquals("FAST", old.getSetting(v2));

        PlayerSetting stored = old.getSettings().get("mode").withUpdatedAt(NOW);
        assertEquals("FAST", stored.value(), "migrated value must be written back");
        assertEquals(2, stored.schema());
        assertEquals(NOW, stored.updatedAt());
    }

    @Test
    void unknownKeysSurviveSettingChanges() {
        SettingKey<String> legacy = SettingKey.builder(String.class, "legacy").defaultValue("").build();
        player.setSetting(legacy, "keep");
        player.setSetting(flying, true);
        player.setSetting(flying, false);

        assertEquals("keep", player.getSettings().get("legacy").value());
    }

    @Test
    void unparsableStoredValueFallsBackToDefault() {
        SettingKey<Integer> count = SettingKey.builder(Integer.class, "count").defaultValue(3).build();
        SettingKey<String> asString = SettingKey.builder(String.class, "count").defaultValue("").build();
        player.setSetting(asString, "abc");

        assertEquals(3, player.getSetting(count));
    }

    @Test
    void equalityIsBasedOnMinecraftUuid() {
        UUID playerAId = UUID.randomUUID();
        UUID playerBId = UUID.randomUUID();
        UUID uuid = UUID.randomUUID();
        assertEquals(new StardustPlayer(playerAId, uuid, "A", NOW, NOW).getUniqueId(), new StardustPlayer(playerBId, uuid, "B", NOW, NOW).getUniqueId());
    }
}
