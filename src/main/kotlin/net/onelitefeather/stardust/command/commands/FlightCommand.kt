package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.miniMessage
import org.bukkit.GameMode
import org.bukkit.command.CommandSender

class FlightCommand(val stardustPlugin: StardustPlugin) {

    @CommandMethod("flight|fly [player]")
    @CommandPermission("featheressentials.command.flight")
    @CommandDescription("Allows a Player to flight.")
    fun handleFlightCommand(
        commandSender: CommandSender,
        @Greedy @Argument(value = "player", suggestions = "players") targetName: String?
    ) {

        val playerName = targetName ?: commandSender.name
        val targetPlayer = commandSender.server.getPlayer(playerName)

        if (targetPlayer == null) {
            commandSender.sendMessage(miniMessage {
                stardustPlugin.getMessage(
                    "plugin.player-not-found",
                    this.stardustPlugin.getPrefix(),
                    playerName
                )
            })
            return
        }

        val user = this.stardustPlugin.userService.getUser(target.getUniqueId())
        val displayName = this.stardustPlugin.getVaultHook().getPlayerDisplayName(target)

        if (GameMode.CREATIVE == targetPlayer.gameMode || GameMode.SPECTATOR == targetPlayer.gameMode) {
            commandSender.sendMessage(
                miniMessage {
                    this.stardustPlugin.getMessage(
                        "commands.flight.already-flying",
                        this.stardustPlugin.getPrefix(),
                        user.getDisplayName()
                    )
                }
            )
            return
        }

        val enabled = this.stardustPlugin.getMessage(
            "commands.flight.enable",
            this.stardustPlugin.getPrefix(),
            displayName
        )
        val disabled = this.stardustPlugin.getMessage(
            "commands.flight.disable",
            this.stardustPlugin.getPrefix(),
            displayName
        )

        if (commandSender != targetPlayer && !commandSender.hasPermission("featheressentials.command.flight.others")) {
            commandSender.sendMessage(miniMessage {
                this.stardustPlugin.getMessage(
                    "plugin.not-enough-permissions",
                    this.stardustPlugin.getPrefix()
                )
            })
            return
        }

        targetPlayer.allowFlight = !targetPlayer.allowFlight
        user.setFlying(targetPlayer.allowFlight)
        commandSender.sendMessage(miniMessage { if (targetPlayer.allowFlight) enabled else disabled })

        if (commandSender != targetPlayer) {
            targetPlayer.sendMessage(miniMessage { if (targetPlayer.allowFlight) enabled else disabled })
        }
    }
}