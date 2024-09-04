package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.kyori.adventure.text.Component
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

            val enabledMessage = Component.translatable("commands.flight.enable").arguments(stardustPlugin.getPluginPrefix(), target.displayName())
            val disabledMessage = Component.translatable("commands.flight.disable").arguments(stardustPlugin.getPluginPrefix(), target.displayName())

            if (commandSender != target && !commandSender.hasPermission("stardust.command.flight.others")) {
                commandSender.sendMessage(Component.translatable("plugin.not-enough-permissions").arguments(stardustPlugin.getPluginPrefix()))
                return
            }

            if (target.gameMode == GameMode.CREATIVE) {
                commandSender.sendMessage(Component.translatable("commands.flight.already-in-creative").arguments(
                    stardustPlugin.getPluginPrefix(),
                    target.displayName()))
                return
            }

            target.allowFlight = !target.allowFlight
            stardustPlugin.userService.setUserProperty(user, UserPropertyType.FLYING, target.allowFlight)
            commandSender.sendMessage(if (target.allowFlight) enabledMessage else disabledMessage   )

            if (commandSender != target) {
                target.sendMessage(if (target.allowFlight) enabledMessage else disabledMessage )
            }
        } catch (e: Exception) {
            this.stardustPlugin.logger.throwing(FlightCommand::class.java.simpleName, "handleFlight", e)
        }
    }
}