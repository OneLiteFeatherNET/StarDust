package net.onelitefeather.stardust.task;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.util.PlayerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StardustPluginTask implements Runnable {

    private final StardustPlugin plugin;

    public StardustPluginTask(@NotNull StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getServer().getOnlinePlayers().forEach(player -> {

            if (player.isInvulnerable()) saturatePlayer(player);

            var stardustPlayer = plugin.getPlayerService().findById(player.getUniqueId());
            if (stardustPlayer == null) return;

            if (stardustPlayer.isVanished()) {
                player.sendActionBar(Component.translatable("plugin.vanish-actionbar"));
                saturatePlayer(player);
                //Keep fly mode if the player's gamemode was changed to survival or adventure
                if (!player.getAllowFlight() && !PlayerUtil.canEnterFlyMode(player)) {
                    player.setAllowFlight(true);
                }

                return;
            }

            if (!PlayerUtil.canEnterFlyMode(player)) {
                player.setAllowFlight(stardustPlayer.allowedFlight());
            }
        });
    }

    private void saturatePlayer(Player player) {
        int requiredFoodLevel = 3; //Required food level for sprinting
        if(player.getFoodLevel() <= requiredFoodLevel) {
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.setExhaustion(0);
        }
    }
}
