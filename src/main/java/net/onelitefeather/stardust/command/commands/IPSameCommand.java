package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

public final class IPSameCommand {


    private final StardustPlugin plugin;

    public IPSameCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("ipsame <player>")
    @Permission(value = {
            "stardust.command.ipsame",
            "psittaciforms.command.ipsame"
    })
    @CommandDescription("Check if the player is on the same IP as another player.")
    public void isPlayerIPSame(Player player, @Argument("player") Player target) {
        if (target.getAddress() == null || player == target) return;
        player.sendMessage(Component.translatable("commands.ipsame.target-ip").arguments(plugin.getPrefix(), target.displayName(), Component.text(target.getAddress().getHostString())));
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer == target) {
                continue;
            }
            if (onlinePlayer.getAddress() == null) {
                continue;
            }
            if (!onlinePlayer.getAddress().getHostString().equals(target.getAddress().getHostString())) {
                continue;
            }
            player.sendMessage(Component.translatable("commands.ipsame.show").arguments(plugin.getPrefix(), target.displayName(), onlinePlayer.displayName()));
        }
    }

}
