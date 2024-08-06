package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class HealCommand(private val stardustPlugin: StardustPlugin) : PlayerUtils {

    @CommandMethod("heal [player]")
    @CommandPermission("stardust.command.heal")
    @CommandDescription("Heal a player.")
    fun onCommand(commandSender: CommandSender, @Greedy @Argument(value = "player") target: Player?) {
        if (commandSender is Player) {
            healPlayer(commandSender, target ?: commandSender)
        } else {
            if (target == null) return
            healPlayer(commandSender, target)
        }
    }

    private fun healPlayer(commandSender: CommandSender, target: Player) {

        try {
            if (target != commandSender && !commandSender.hasPermission("stardust.command.heal.others")) {
                commandSender.sendMessage(Component.translatable("plugin.not-enough-permissions").arguments(stardustPlugin.getPluginPrefix()))
                return
            }

            val healthAttribute: AttributeInstance? = target.getAttribute(Attribute.GENERIC_MAX_HEALTH)
            if (healthAttribute != null) {
                target.health = healthAttribute.value
            }

            target.fireTicks = DEFAULT_PLAYER_FIRE_TICKS
            target.isVisualFire = DEFAULT_ENTITY_HAS_VISUAL_FIRE
            target.foodLevel = DEFAULT_PLAYER_FOOD_LEVEL
            target.saturation = DEFAULT_PLAYER_SATURATION_LEVEL

            val message = Component.translatable("commands.heal.success").arguments(
                stardustPlugin.getPluginPrefix(),
                target.displayName(),
                Component.text(target.health))

            if (commandSender != target) {
                target.sendMessage(message)
            }

            commandSender.sendMessage(message)
        } catch (e: Exception) {
            this.stardustPlugin.getLogger().throwing(HealCommand::class.java.simpleName, "healPlayer", e)
        }
    }
}