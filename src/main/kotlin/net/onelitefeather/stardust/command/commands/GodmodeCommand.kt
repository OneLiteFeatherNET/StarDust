package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.PlayerUtils
import net.onelitefeather.stardust.util.RADIUS_REMOVE_ENEMIES
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GodmodeCommand(private val stardustPlugin: StardustPlugin) : PlayerUtils {

    @CommandMethod("godmode [player]")
    @CommandPermission("stardust.command.godmode")
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

            val enabledMessage = Component.translatable("commands.god-mode.enable").arguments(stardustPlugin.getPluginPrefix(), target.displayName())
            val disabledMessage = Component.translatable("commands.god-mode.disable").arguments(stardustPlugin.getPluginPrefix(), target.displayName())

            target.sendMessage(if (target.isInvulnerable) enabledMessage else disabledMessage)
            if (commandSender != target) {
                commandSender.sendMessage(if (target.isInvulnerable) enabledMessage else disabledMessage)
            }
        } catch (e: Exception) {
            this.stardustPlugin.getLogger().throwing(GodmodeCommand::class.java.simpleName, "handleInvulnerability", e)
        }
    }
}