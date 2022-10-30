package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.miniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class VanishCommand(private val stardustPlugin: StardustPlugin) {

    @CommandMethod("vanish|v [player]")
    @CommandPermission("stardust.command.vanish")
    @CommandDescription("Make a player invisible for other players")
    fun handleCommand(commandSender: CommandSender, @Greedy @Argument(value = "player") target: Player?) {
        if (target == null) {
            toggleVanish(commandSender, commandSender as Player)
        } else {
            toggleVanish(commandSender, target)
        }
    }

    fun toggleVanish(commandSender: CommandSender, target: Player) {

        if (target != commandSender && !commandSender.hasPermission("stardust.command.vanish.others")) {
            commandSender.sendMessage(miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "plugin.not-enough-permissions",
                    *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
                )
            })
            return
        }

        val user = stardustPlugin.userService.getUser(target.uniqueId)
        if (user != null) {

            val state = stardustPlugin.userService.toggleVanish(user)
            val targetEnable = stardustPlugin.i18nService.getMessage(
                "commands.vanish.enable",
                *arrayOf(stardustPlugin.i18nService.getPluginPrefix(), user.getDisplayName())
            )
            val targetDisable = stardustPlugin.i18nService.getMessage(
                "commands.vanish.disable",
                *arrayOf(stardustPlugin.i18nService.getPluginPrefix(), user.getDisplayName())
            )

            if (commandSender != target) {
                commandSender.sendMessage(miniMessage { if (state) targetEnable else targetDisable })
            }

            target.sendMessage(miniMessage { if (state) targetEnable else targetDisable })
        }
    }
}