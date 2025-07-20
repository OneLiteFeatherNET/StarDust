package net.onelitefeather.stardust.command.commands;

import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class InvSeeCommand {

    @Command("invsee <player>")
    @Permission(value = {
            "stardust.command.invsee",
            "psittaciforms.command.invsee"
    })
    public void invSeePlayer(Player player, @Argument("player") Player target) {
        player.openInventory(target.getInventory());
    }
}
