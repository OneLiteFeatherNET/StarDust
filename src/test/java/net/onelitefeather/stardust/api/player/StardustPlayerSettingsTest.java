package net.onelitefeather.stardust.api.player;

import net.onelitefeather.stardust.api.player.setting.PlayerSetting;
import net.onelitefeather.stardust.api.player.setting.SettingKey;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StardustPlayerSettingsTest {

    private static final Instant NOW = Instant.parse("2026-01-02T03:04:05Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final StardustPlayer player = new StardustPlayer(UUID.randomUUID(), "Steve", clock);

    private final SettingKey<Boolean> flying = SettingKey.builder(Boolean.class, "flying")
            .defaultValue(false).schemaVersion(1).build();

    @Test
    void returnsDefaultWhenSettingIsAbsent() {
        assertEquals(false, player.getSetting(flying), "absent setting must yield the default");
    }

    @Test
    void storesNonDefaultValueWithSchemaAndClockTime() {
        player.setSetting(flying, true);

        PlayerSetting stored = player.getRawSettings().get("flying");
        assertNotNull(stored, "non-default value must be persisted");
        assertEquals("true", stored.value());
        assertEquals(1, stored.schema());
        assertEquals(NOW, stored.updatedAt());
        assertTrue(player.getSetting(flying));
    }

    @Test
    void removesEntryWhenValueEqualsDefault() {
        player.setSetting(flying, true);
        player.setSetting(flying, false);

        assertFalse(player.getRawSettings().containsKey("flying"), "default values must not be stored");
    }

    @Test
    void nullValueResetsToDefault() {
        player.setSetting(flying, true);
        player.setSetting(flying, null);

        assertTrue(player.getRawSettings().isEmpty());
    }

    @Test
    void appliesMigrationWhenStoredSchemaIsOlder() {
        SettingKey<String> v1 = SettingKey.builder(String.class, "mode").defaultValue("none").schemaVersion(1).build();
        SettingKey<String> v2 = SettingKey.builder(String.class, "mode")
                .defaultValue("none").schemaVersion(2).migration(String::toUpperCase).build();
        Clock earlier = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        StardustPlayer old = new StardustPlayer(UUID.randomUUID(), "Alex", earlier);
        old.setSetting(v1, "fast");
        old.useClock(clock);

        assertEquals("FAST", old.getSetting(v2));
        PlayerSetting stored = old.getRawSettings().get("mode");
        assertEquals("FAST", stored.value(), "migrated value must be written back");
        assertEquals(2, stored.schema());
        assertEquals(NOW, stored.updatedAt());
    }

    @Test
    void migrationToDefaultRemovesEntry() {
        SettingKey<String> v1 = SettingKey.builder(String.class, "mode").defaultValue("none").schemaVersion(1).build();
        SettingKey<String> v2 = SettingKey.builder(String.class, "mode")
                .defaultValue("none").schemaVersion(2).migration(v -> "none").build();
        player.setSetting(v1, "fast");

        assertEquals("none", player.getSetting(v2));
        assertTrue(player.getRawSettings().isEmpty());
    }

    @Test
    void unknownKeysSurviveSettingChanges() {
        SettingKey<String> legacy = SettingKey.builder(String.class, "legacy").defaultValue("").build();
        player.setSetting(legacy, "keep");

        player.setSetting(flying, true);
        player.setSetting(flying, false);

        assertEquals("keep", player.getRawSettings().get("legacy").value());
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
        UUID uuid = UUID.randomUUID();
        assertEquals(new StardustPlayer(uuid, "A"), new StardustPlayer(uuid, "B"));
        assertEquals(new StardustPlayer(uuid, "A").hashCode(), new StardustPlayer(uuid, "B").hashCode());
    }
}
