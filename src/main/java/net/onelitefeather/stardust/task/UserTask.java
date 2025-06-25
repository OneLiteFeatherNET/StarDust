package net.onelitefeather.stardust.task;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.util.PlayerUtil;
import org.jetbrains.annotations.NotNull;

public class UserTask implements Runnable {

    private final StardustPlugin plugin;

    public UserTask(@NotNull StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getServer().getOnlinePlayers().forEach(player -> {

            var user = plugin.getUserService().getUser(player.getUniqueId());
            if (user == null) return;

            if (user.isVanished()) {
                player.sendActionBar(Component.translatable("plugin.vanish-actionbar"));

                //Keep fly mode if the player's gamemode was changed to survival or adventure
                if (!player.getAllowFlight() && !PlayerUtil.canEnterFlyMode(player)) {
                    player.setAllowFlight(true);
                }

                return;
            }

            if (!PlayerUtil.canEnterFlyMode(player)) {
                player.setAllowFlight(user.isFlying());
            }
        });
    }
}
