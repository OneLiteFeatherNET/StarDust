package net.onelitefeather.stardust.api.database;

import jakarta.persistence.*;
import net.onelitefeather.stardust.api.player.setting.PlayerSetting;
import net.onelitefeather.stardust.api.player.setting.SettingKey;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

/**
 * A player known to StarDust. Settings are stored as a JSON map keyed by {@link SettingKey#key()};
 * only values that differ from {@link SettingKey#defaultValue()} are persisted.
 */
@Entity
@Table(name = "stardust_players")
public class StardustPlayerEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "stardust_id", nullable = false, updatable = false)
    private UUID id;

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

    public StardustPlayerEntity() {
        // Empty constructor for hibernate
    }

    public StardustPlayerEntity(UUID id,
                                UUID uuid,
                                String name,
                                Map<String, PlayerSetting> settings,
                                Instant createdAt,
                                Instant updatedAt) {
        this.id = id;
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.name = Objects.requireNonNull(name, "name");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getVersion() {
        return version;
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

    /**
     * The persisted settings, including entries whose key is not (or no longer) registered.
     */
    public Map<String, PlayerSetting> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, PlayerSetting> settings) {
        this.settings = settings;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof StardustPlayerEntity that)) return false;

        return version == that.version &&
                Objects.equals(id, that.id) &&
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(name, that.name) && Objects.equals(settings, that.settings) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(uuid);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(settings);
        result = 31 * result + Long.hashCode(version);
        result = 31 * result + Objects.hashCode(createdAt);
        result = 31 * result + Objects.hashCode(updatedAt);
        return result;
    }
}
