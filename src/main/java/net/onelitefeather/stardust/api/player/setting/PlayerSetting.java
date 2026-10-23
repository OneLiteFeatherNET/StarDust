package net.onelitefeather.stardust.api.player.setting;

import java.time.Instant;
import java.util.Objects;

public class PlayerSetting {

    private Long id;
    private SettingKey<?> settingKey;
    private String value;
    private Integer schemaVersion;
    private Instant updatedAt;
    private Long version;

    public PlayerSetting(Long id, SettingKey<?> settingKey, String value, Integer schemaVersion, Instant updatedAt, Long version) {
        this.id = id;
        this.settingKey = settingKey;
        this.value = value;
        this.schemaVersion = schemaVersion;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SettingKey<?> getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(SettingKey<?> settingKey) {
        this.settingKey = settingKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof PlayerSetting that)) return false;

        return Objects.equals(id, that.id) &&
                Objects.equals(settingKey, that.settingKey) &&
                Objects.equals(value, that.value) &&
                Objects.equals(schemaVersion, that.schemaVersion) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(settingKey);
        result = 31 * result + Objects.hashCode(value);
        result = 31 * result + Objects.hashCode(schemaVersion);
        result = 31 * result + Objects.hashCode(updatedAt);
        result = 31 * result + Objects.hashCode(version);
        return result;
    }

    @Override
    public String toString() {
        return "UserSetting{" +
                "id=" + id +
                ", settingKey=" + settingKey +
                ", value='" + value + '\'' +
                ", schemaVersion=" + schemaVersion +
                ", updatedAt=" + updatedAt +
                ", version=" + version +
                '}';
    }
}
