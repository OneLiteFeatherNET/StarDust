package net.onelitefeather.stardust.api.player.setting;

import net.onelitefeather.stardust.api.player.StardustPlayer;

import java.time.Instant;
import java.util.Objects;

/**
 * One persisted setting value inside the {@code settings} JSON column of a
 * {@link StardustPlayer}. The setting key is the key of the
 * surrounding map, so it is not repeated here.
 *
 */
public final class PlayerSetting {
    private final String value;
    private final int schema;
    private Instant updatedAt;

    /**
     * @param value     the value in its string form
     * @param schema    the {@link SettingKey#schemaVersion()} the value was written with
     * @param updatedAt when the value was last written
     */
    public PlayerSetting(String value, int schema, Instant updatedAt) {
        this.value = value;
        this.schema = schema;
        this.updatedAt = updatedAt;
    }

    public String value() {
        return value;
    }

    public int schema() {
        return schema;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public PlayerSetting withUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PlayerSetting) obj;
        return Objects.equals(this.value, that.value) &&
                this.schema == that.schema &&
                Objects.equals(this.updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, schema, updatedAt);
    }

    @Override
    public String toString() {
        return "PlayerSetting[" +
                "value=" + value + ", " +
                "schema=" + schema + ", " +
                "updatedAt=" + updatedAt + ']';
    }

}
