package net.onelitefeather.stardust.command.commands

import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.PlayerUtils
import net.onelitefeather.stardust.util.RADIUS_REMOVE_ENEMIES
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

class GodmodeCommand(private val stardustPlugin: StardustPlugin) : PlayerUtils {

    @Command("godmode [player]")
    @Permission("stardust.command.godmode")
    @CommandDescription("Makes a player invulnerable to everything")
    fun handleCommand(commandSender: CommandSender, @Greedy @Argument(value = "player") target: Player?) {
        handleInvulnerability(commandSender, target ?: commandSender as Player)
    }

    private fun handleInvulnerability(commandSender: CommandSender, target: Player) {

        try {
            if (target != commandSender && !commandSender.hasPermission("stardust.command.godmode.others")) {
                commandSender.sendMessage(Component.translatable("plugin.not-enough-permissions").arguments(stardustPlugin.getPluginPrefix()))
                return
            }

            target.isInvulnerable = !target.isInvulnerable
            removeEnemies(target, RADIUS_REMOVE_ENEMIES)

            val targetEnabledMessage = Component.translatable("commands.godmode.enable.target").arguments(stardustPlugin.getPluginPrefix())
            val targetDisabledMessage = Component.translatable("commands.godmode.disable.target").arguments(stardustPlugin.getPluginPrefix())

            val enabledMessage = Component.translatable("commands.godmode.enable").arguments(stardustPlugin.getPluginPrefix(), target.displayName())
            val disabledMessage = Component.translatable("commands.godmode.disable").arguments(stardustPlugin.getPluginPrefix(), target.displayName())

            if(commandSender == target) {
                target.sendMessage(if (target.isInvulnerable) targetEnabledMessage else targetDisabledMessage)
            } else {
                commandSender.sendMessage(if (target.isInvulnerable) enabledMessage else disabledMessage)
                target.sendMessage(if (target.isInvulnerable) targetEnabledMessage else targetDisabledMessage)
            }
        } catch (e: Exception) {
            this.stardustPlugin.logger.throwing(GodmodeCommand::class.java.simpleName, "handleInvulnerability", e)
        }
    }
}