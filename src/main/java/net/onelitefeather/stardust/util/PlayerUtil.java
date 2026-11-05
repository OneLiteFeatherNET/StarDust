package net.onelitefeather.stardust.util;

import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.player.setting.PlayerSetting;
import net.onelitefeather.stardust.api.registry.SettingRegistry;
import org.bukkit.GameMode;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class PlayerUtil {

    private PlayerUtil() {
        throw new UnsupportedOperationException("Utility Class!");
    }

    public static Map<String, PlayerSetting> findSettingsForPlayer(Player player) {
        Map<String, PlayerSetting> playerSettings = new HashMap<>();
        SettingRegistry.getDefaults().forEach((key, settingKey) -> {
            if (settingKey.hasPermission()) {
                if (player.hasPermission(settingKey.permission())) {
                    playerSettings.put(key, SettingRegistry.fromKey(settingKey));
                }
            } else {
                playerSettings.put(key, SettingRegistry.fromKey(settingKey));
            }
        });

        return playerSettings;

    }

    public static boolean canEnterFlyMode(Player player) {
        return player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
    }

    /**
     * Remove the player as target for attack/look from the mobs in definied radius.
     * Prevents looks from entities to the player.
     **/
    public static void removeEnemies(Player player, double radius) {
        if (Constants.INSIDE_TEST) return;
        var plugin = JavaPlugin.getPlugin(StardustPlugin.class);
        player.getServer().getScheduler().getMainThreadExecutor(plugin).execute(() ->
                player.getNearbyEntities(radius, radius, radius).forEach(entity -> {
                    if (entity instanceof Mob mob) {
                        var target = mob.getTarget();
                        if (target == null) return;
                        if (target == player) {
                            mob.setTarget(null);
                        }
                    }
                }));
    }
}
