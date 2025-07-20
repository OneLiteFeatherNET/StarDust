package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public final class IpShowCommand {

    private final StardustPlugin plugin;

    public IpShowCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("ipshow [player]")
    @Permission(value = {
            "stardust.command.ipshow",
            "psittaciforms.command.ipshow"
    })
    public void ipShowPlayer(Player player, @Greedy @Argument("player") Player target) {
        if (target != null && player.hasPermission("stardust.command.ipshow.other")) {
            if (target.getAddress() != null) {
                player.sendMessage(Component.translatable("stardust.command.ipshow").arguments(
                        plugin.getPrefix(),
                        target.displayName(),
                        Component.text(target.getAddress().getHostName()),
                        Component.text(target.getAddress().getPort())));
            }
        } else {
            if (player.getAddress() != null) {
                player.sendMessage(Component.translatable("stardust.command.ipshow").arguments(
                        plugin.getPrefix(),
                        player.displayName(),
                        Component.text(player.getAddress().getHostName()),
                        Component.text(player.getAddress().getPort())));
            }
        }
    }
}
