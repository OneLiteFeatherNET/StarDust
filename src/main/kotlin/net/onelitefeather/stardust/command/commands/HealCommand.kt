package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.miniMessage
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class HealCommand(private val stardustPlugin: StardustPlugin) {

    @CommandMethod("heal [player]")
    @CommandPermission("stardust.command.heal")
    @CommandDescription("Heal a Player.")
    fun onCommand(commandSender: CommandSender, @Greedy @Argument(value = "player") target: Player?) {
        if (target == null) {
            commandSender.sendMessage(miniMessage { stardustPlugin.i18nService.getMessage("plugin.player-not-found") })
            return
        }

        healPlayer(commandSender, target)
    }

    private fun healPlayer(commandSender: CommandSender, target: Player) {

        if (target != commandSender && !commandSender.hasPermission("stardust.command.heal.others")) {
            commandSender.sendMessage(miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "plugin.not-enough-permissions",
                    *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
                )
            })

            return
        }

        val healthAttribute: AttributeInstance? = target.getAttribute(Attribute.GENERIC_MAX_HEALTH)
        if (healthAttribute != null) {
            target.health = healthAttribute.value
        }

        target.fireTicks = 0
        target.isVisualFire = false
        target.foodLevel = 20
        target.saturation = 20.0F

        val message = this.stardustPlugin.i18nService.getMessage(
            "commands.heal.success",
            stardustPlugin.i18nService.getPluginPrefix(),
            stardustPlugin.i18nService.translateLegacyString(target.displayName()),
            target.health
        )

        if (commandSender != target) {
            target.sendMessage(miniMessage { message })
        }

        commandSender.sendMessage(miniMessage { message })
    }
}