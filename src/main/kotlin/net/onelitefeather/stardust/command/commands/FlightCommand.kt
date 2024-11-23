package net.onelitefeather.stardust.command.commands

import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.user.UserPropertyType
import net.onelitefeather.stardust.util.PlayerUtils
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

class FlightCommand(val stardustPlugin: StardustPlugin) : PlayerUtils {

    @Command("flight|fly [player]")
    @Permission("stardust.command.flight")
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

            if (commandSender != target && !commandSender.hasPermission("stardust.command.flight.others")) {
                commandSender.sendMessage(
                    Component.translatable("plugin.not-enough-permissions").arguments(stardustPlugin.getPluginPrefix())
                )
                return
            }

            if (canEnterFlyMode(target)) {
                commandSender.sendMessage(
                    Component.translatable("already-in-flight-mode").arguments(
                        stardustPlugin.getPluginPrefix(),
                        target.displayName()
                    )
                )
                return
            }

            target.allowFlight = !target.allowFlight
            stardustPlugin.userService.setUserProperty(user, UserPropertyType.FLYING, target.allowFlight)

            val targetEnabledMessage = Component.translatable("commands.flight.target.enable").arguments(stardustPlugin.getPluginPrefix())
            val targetDisabledMessage = Component.translatable("commands.flight.target.disable").arguments(stardustPlugin.getPluginPrefix())

            val enabledMessage = Component.translatable("commands.flight.enable")
                .arguments(stardustPlugin.getPluginPrefix(), target.displayName())
            val disabledMessage = Component.translatable("commands.flight.disable")
                .arguments(stardustPlugin.getPluginPrefix(), target.displayName())

            if(commandSender == target) {
                target.sendMessage(if (target.allowFlight) targetEnabledMessage else targetDisabledMessage)
            } else {
                commandSender.sendMessage(if (target.allowFlight) enabledMessage else disabledMessage)
                target.sendMessage(if (target.allowFlight) targetEnabledMessage else targetDisabledMessage)
            }
        } catch (e: Exception) {
            this.stardustPlugin.logger.throwing(FlightCommand::class.java.simpleName, "handleFlight", e)
        }
    }
}