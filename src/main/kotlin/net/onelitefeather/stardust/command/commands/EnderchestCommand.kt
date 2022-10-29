package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.miniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class EnderchestCommand(val stardustPlugin: StardustPlugin) {


    @CommandMethod("enderchest|ec [player]")
    @CommandPermission("featheressentials.command.enderchest")
    @CommandDescription("Open the Enderchest from another player")
    fun handleCommand(
        commandSender: CommandSender,
        @Greedy @Argument(value = "player", suggestions = "players") targetName: String?
    ) {

        if (commandSender !is Player) {
            commandSender.sendMessage(miniMessage { stardustPlugin.getMessage("plugin.only-player-command") });
            return
        }

        val playerName = targetName ?: commandSender.name
        val targetPlayer = stardustPlugin.server.getPlayer(playerName)

        if (targetPlayer == null) {
            commandSender.sendMessage(miniMessage {
                stardustPlugin.getMessage(
                    "plugin.player-not-found",
                    stardustPlugin.getPrefix(),
                    playerName
                )
            })
            return
        }

        if (commandSender != targetPlayer && !commandSender.hasPermission("featheressentials.command.enderchest.others")) {
            commandSender.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    stardustPlugin.getMessage(
                        "plugin.not-enough-permissions",
                        stardustPlugin.getPrefix()
                    )
                )
            )
            return
        }

        commandSender.openInventory(targetPlayer.enderChest)
    }
}