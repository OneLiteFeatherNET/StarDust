package net.onelitefeather.stardust.api.player;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import net.onelitefeather.stardust.api.player.setting.PlayerSetting;
import net.onelitefeather.stardust.api.player.setting.SettingKey;
import net.onelitefeather.stardust.api.player.setting.SettingValueCodec;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A player known to StarDust. Settings are stored as a JSON map keyed by {@link SettingKey#key()};
 * only values that differ from {@link SettingKey#defaultValue()} are persisted.
 */
@Entity
@Table(name = "stardust_player")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "stardust-player")
@NaturalIdCache(region = "stardust-player-naturalid")
public class StardustPlayer {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "stardust_id", nullable = false, updatable = false)
    private UUID stardustId;

    @NaturalId
    @Column(name = "minecraft_uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(name = "name", nullable = false, length = 16)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "settings", nullable = false)
    private Map<String, PlayerSetting> settings = new HashMap<>();

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Transient
    private transient Clock clock = Clock.systemUTC();

    public StardustPlayer() {
        // Empty constructor for hibernate
    }

    public StardustPlayer(UUID uuid, String name) {
        this(uuid, name, Clock.systemUTC());
    }

    public StardustPlayer(UUID uuid, String name, Clock clock) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.name = Objects.requireNonNull(name, "name");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public UUID getStardustId() {
        return stardustId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Replaces the clock used for {@code updatedAt} of setting entries (runtime only, not persisted).
     */
    public void useClock(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * The persisted settings, including entries whose key is not (or no longer) registered.
     */
    public Map<String, PlayerSetting> getRawSettings() {
        return Collections.unmodifiableMap(settings);
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
     * Stores the value, or removes the entry if it equals the default. A {@code null} value resets
     * the setting to its default.
     */
    public <T> void setSetting(SettingKey<T> key, T value) {
        if (value == null || Objects.equals(value, key.defaultValue())) {
            settings.remove(key.key());
            return;
        }
        settings.put(key.key(), new PlayerSetting(SettingValueCodec.encode(value), key.schemaVersion(), clock.instant()));
    }

    private <T> PlayerSetting migrate(SettingKey<T> key, PlayerSetting stored) {
        String value = stored.value();
        if (key.migrationFunction() != null) {
            value = key.migrationFunction().apply(value);
        }
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
        PlayerSetting migrated = new PlayerSetting(value, key.schemaVersion(), clock.instant());
        settings.put(key.key(), migrated);
        return migrated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StardustPlayer other)) return false;
        return uuid != null && uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }
}
