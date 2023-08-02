package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.user.UserPropertyType
import net.onelitefeather.stardust.util.PlayerUtils
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FlightCommand(val stardustPlugin: StardustPlugin) : PlayerUtils {

    @CommandMethod("flight|fly [player]")
    @CommandPermission("stardust.command.flight")
    @CommandDescription("Allows a player to flight.")
    fun handleFlightCommand(commandSender: CommandSender, @Greedy @Argument(value = "player") target: Player?) {

        if (commandSender is Player && target == null) {
            handleFlight(commandSender, commandSender)
            return
        }

        if (target != null) {
            handleFlight(commandSender, target)
        }
    }

    private fun handleFlight(commandSender: CommandSender, target: Player) {

        try {
            val user = stardustPlugin.userService.getUser(target.uniqueId)!!

            val enabledMessage =
                "<lang:commands.flight.enable:'${stardustPlugin.getPluginPrefix()}':'${coloredDisplayName(target)}'>"
            val disabledMessage =
                "<lang:commands.flight.disable:'${stardustPlugin.getPluginPrefix()}':'${coloredDisplayName(target)}'>"

            if (commandSender != target && !commandSender.hasPermission("stardust.command.flight.others")) {
                commandSender.sendMessage(
                    MiniMessage.miniMessage()
                        .deserialize("<lang:plugin.not-enough-permissions:'${stardustPlugin.getPluginPrefix()}'>")
                )
                return
            }

            if (target.gameMode == GameMode.CREATIVE) {
                commandSender.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                        "<lang:commands.flight.already-in-creative:'${stardustPlugin.getPluginPrefix()}':'${
                            coloredDisplayName(
                                target
                            )
                        }'>"

                    )
                )
                return
            }

            target.allowFlight = !target.allowFlight
            stardustPlugin.userService.setUserProperty(user, UserPropertyType.FLYING, target.allowFlight)
            commandSender.sendMessage(
                MiniMessage.miniMessage().deserialize(if (target.allowFlight) enabledMessage else disabledMessage)
            )

            if (commandSender != target) {
                target.sendMessage(
                    MiniMessage.miniMessage().deserialize(if (target.allowFlight) enabledMessage else disabledMessage)
                )
            }
        } catch (e: Exception) {
            this.stardustPlugin.getLogger().throwing(FlightCommand::class.java.simpleName, "handleFlight", e)
        }
    }
}