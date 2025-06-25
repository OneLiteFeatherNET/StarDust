package net.onelitefeather.stardust.util;

import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerUtil {

    private PlayerUtil() {
        throw new UnsupportedOperationException("Utility Class!");
    }

    public static boolean canEnterFlyMode(Player player) {
        return player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
    }

    /**
     * Remove the player as target for attack/look from the mobs in definied radius.
     * Prevents looks from entities to the player.
     **/
    public static void removeEnemies(Player player, double radius) {
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
