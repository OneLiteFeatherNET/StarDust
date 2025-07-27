package net.onelitefeather.stardust.command.commands;

import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public final class InvSeeCommand {

    @Command("invsee <player>")
    @Permission(value = {
            "stardust.command.invsee",
            "psittaciforms.command.invsee"
    })
    public void invSeePlayer(Player player, @Argument("player") Player target) {
        StardustPlugin plugin = JavaPlugin.getPlugin(StardustPlugin.class);
        player.getScheduler().run(plugin, scheduledTask -> player.openInventory(target.getInventory()), null);
    }
}
