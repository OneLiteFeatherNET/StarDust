package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public final class PingCommand {

    private final StardustPlugin plugin;

    public PingCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("ping [player]")
    @Permission(value = {
            "stardust.command.ping",
            "psittaciforms.command.ping"
    })
    public void pingPlayer(Player player, @Greedy @Argument("player") Player target) {
        if (target != null && player.hasPermission("stardust.command.ping.other")) {
            if (target.getAddress() != null) {
                player.sendMessage(Component.translatable("commands.ping.show").arguments(
                                plugin.getPrefix(),
                                target.displayName(),
                                TranslationArgument.numeric(target.getPing())));
            }
        } else {
            if (player.getAddress() != null) {
                player.sendMessage(Component.translatable("commands.ping.show").arguments(
                                plugin.getPrefix(),
                                player.displayName(),
                                TranslationArgument.numeric(player.getPing())));
            }
        }
    }
}
