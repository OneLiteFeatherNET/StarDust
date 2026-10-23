package net.onelitefeather.stardust.api.player.setting;

import java.time.Instant;

/**
 * One persisted setting value inside the {@code settings} JSON column of a
 * {@link net.onelitefeather.stardust.api.player.StardustPlayer}. The setting key is the key of the
 * surrounding map, so it is not repeated here.
 *
 * @param value     the value in its string form
 * @param schema    the {@link SettingKey#schemaVersion()} the value was written with
 * @param updatedAt when the value was last written
 */
public record PlayerSetting(String value, int schema, Instant updatedAt) {
}
