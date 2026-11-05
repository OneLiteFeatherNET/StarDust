package net.onelitefeather.stardust.api.player;

import net.onelitefeather.stardust.api.player.setting.PlayerSetting;
import net.onelitefeather.stardust.api.player.setting.SettingKey;
import net.onelitefeather.stardust.api.player.setting.SettingValueCodec;
import net.onelitefeather.stardust.api.registry.SettingRegistry;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

/**
 * A player known to StarDust. Settings are stored as a JSON map keyed by {@link SettingKey#key()};
 * only values that differ from {@link SettingKey#defaultValue()} are persisted.
 */

public class StardustPlayer {

    public static final StardustPlayer DUMMY = StardustPlayer.create(UUID.randomUUID(), "Dummy", Collections.emptyMap());

    private final UUID id;
    private UUID uniqueId;
    private String name;

    private final Map<String, PlayerSetting> settings;
    private Instant createdAt;
    private Instant updatedAt;

    public StardustPlayer(UUID id, UUID uniqueId, String name, Instant createdAt, Instant updatedAt) {
        this(id, uniqueId, name, new HashMap<>(), createdAt, updatedAt);
    }

    public static StardustPlayer create(UUID uniqueId, String name, Map<String, PlayerSetting> settings) {
        var now = Clock.systemUTC().instant();
        return new StardustPlayer(null, uniqueId, name, settings, now, now);
    }

    public StardustPlayer(UUID id,
                          UUID uniqueId,
                          String name,
                          Map<String, PlayerSetting> settings,
                          Instant createdAt,
                          Instant updatedAt) {
        this.id = id;
        this.uniqueId = Objects.requireNonNull(uniqueId, "uuid");
        this.name = Objects.requireNonNull(name, "name");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public UUID getId() {
        return id;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public StardustPlayer withName(String name) {
        this.name = Objects.requireNonNull(name, "name");
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean hasSetting(String key) {
        return this.settings.containsKey(key);
    }

    /**
     * The persisted settings, including entries whose key is not (or no longer) registered.
     */
    public Map<String, PlayerSetting> getSettings() {
        return settings;
    }

    public StardustPlayer withCanBuild(Boolean canBuild) {
        setSetting(SettingRegistry.getSettingKey("vanish_allow_building"), canBuild);
        return this;
    }

    /**
     *
     * @return true if the player is allowed to build while vanished.
     */
    public boolean canBuild() {
        if (!hasSetting("vanish_allow_building")) return true;
        return SettingValueCodec.decode(Boolean.class, this.settings.get("vanish_allow_building").value());
    }

    public StardustPlayer withCanAttack(Boolean canAttack) {
        setSetting(SettingRegistry.getSettingKey("vanish_allow_pve"), canAttack);
        return this;
    }

    /**
     *
     * @return true if the player is allowed to attack other entities while vanished.
     */
    public boolean canAttack() {
        if (!hasSetting("vanish_allow_pve")) return true;
        return SettingValueCodec.decode(Boolean.class, this.settings.get("vanish_allow_pve").value());
    }

    public StardustPlayer withKeepInventory(Boolean keepInventory) {
        setSetting(SettingRegistry.getSettingKey("vanish_keep_inventory"), keepInventory);
        return this;
    }

    /**
     *
     * <b>The game rule keep inventory for vanished players.</b>
     *
     * @return <b>true if the player is not allowed to drop or pickup items.</b>
     */
    public boolean keepInv() {
        if (!hasSetting("vanish_keep_inventory")) return false;
        return SettingValueCodec.decode(Boolean.class, this.settings.get("vanish_keep_inventory").value());
    }

    public StardustPlayer withAllowFlight(Boolean allowFlight) {
        setSetting(SettingRegistry.getSettingKey("allow_flight"), allowFlight);
        return this;
    }

    public boolean allowedFlight() {
        return SettingValueCodec.decode(Boolean.class, this.settings.get("allow_flight").value());
    }

    public StardustPlayer withVanished(Boolean vanished) {
        setSetting(SettingRegistry.getSettingKey("vanished"), vanished);
        return this;
    }

    public boolean isVanished() {
        if (!hasSetting("vanished")) return false;
        return SettingValueCodec.decode(Boolean.class, this.settings.get("vanished").value());
    }

    /**
     * Returns the stored value, migrated lazily if it was written with an older schema, or the
     * key's default if absent or unreadable.
     */
    public <T> T getSetting(SettingKey<T> key) {
        PlayerSetting stored = settings.get(key.key());
        if (stored == null) {
            return key.defaultValue();
        }
        if (stored.schema() < key.schemaVersion()) {
            stored = migrate(key, stored);
            if (stored == null) {
                return key.defaultValue();
            }
        }
        try {
            return SettingValueCodec.decode(key.type(), stored.value());
        } catch (IllegalArgumentException e) {
            return key.defaultValue();
        }
    }

    /**
     * Stores the value to the represented {@link PlayerSetting}
     *
     */
    public <T> void setSetting(SettingKey<T> key, T value) {

        var now = Clock.systemUTC().instant();
        PlayerSetting stored = settings.get(key.key());
        if (stored == null) {
            settings.put(key.key(), new PlayerSetting(SettingValueCodec.encode(value), key.schemaVersion(), now));
            return;
        }

        this.settings.put(key.key(), new PlayerSetting(SettingValueCodec.encode(value), key.schemaVersion(), now));
    }

    private <T> PlayerSetting migrate(SettingKey<T> key, PlayerSetting stored) {
        String value = stored.value();
        if (key.migrationFunction() != null) value = key.migrationFunction().apply(value);
        try {
            T decoded = SettingValueCodec.decode(key.type(), value);
            if (Objects.equals(decoded, key.defaultValue())) {
                settings.remove(key.key());
                return null;
            }
        } catch (IllegalArgumentException e) {
            settings.remove(key.key());
            return null;
        }
        PlayerSetting migrated = new PlayerSetting(value, key.schemaVersion(), Clock.systemUTC().instant());
        settings.put(key.key(), migrated);
        return migrated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StardustPlayer other)) return false;
        return uniqueId != null && uniqueId.equals(other.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId != null ? uniqueId.hashCode() : 0;
    }
}
