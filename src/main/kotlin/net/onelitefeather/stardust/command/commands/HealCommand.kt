package net.onelitefeather.stardust.command.commands

import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

class HealCommand(private val stardustPlugin: StardustPlugin) : PlayerUtils {

    @Command("heal [player]")
    @Permission("stardust.command.heal")
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

            if (commandSender == target) {
                target.sendMessage(Component.translatable("commands.heal.target").arguments(stardustPlugin.getPluginPrefix()))
            } else {

                target.sendMessage(Component.translatable("commands.heal.target").arguments(
                    stardustPlugin.getPluginPrefix()))

                commandSender.sendMessage(Component.translatable("commands.heal.success").arguments(
                    stardustPlugin.getPluginPrefix(), target.displayName()))
            }

        } catch (e: Exception) {
            this.stardustPlugin.logger.throwing(HealCommand::class.java.simpleName, "healPlayer", e)
        }
    }
}