package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.PlayerUtils
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GlowCommand(private val stardustPlugin: StardustPlugin) : PlayerUtils {

    @CommandMethod("glow [player]")
    @CommandPermission("stardust.command.glow")
    @CommandDescription("Makes a player glowing in his scoreboard team color.")
    fun handleCommand(commandSender: CommandSender, @Greedy @Argument(value = "player") target: Player?) {
        if (target == null) {
            handleGlow(commandSender, commandSender as Player)
        } else {
            handleGlow(commandSender, target)
        }
    }

    private fun handleGlow(commandSender: CommandSender, target: Player) {

        try {
            if (commandSender != target && !commandSender.hasPermission("stardust.command.glow.others")) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<lang:plugin.not-enough-permissions:'${stardustPlugin.getPluginPrefix()}'>"))
                return
            }
            val enabledMessage = "<lang:commands.glow.enabled:'${stardustPlugin.getPluginPrefix()}':'${coloredDisplayName(target)}'>"
            val disabledMessage = "<lang:commands.glow.disabled:'${stardustPlugin.getPluginPrefix()}':'${coloredDisplayName(target)}'>"
            target.isGlowing = !target.isGlowing
            commandSender.sendMessage(
                MiniMessage.miniMessage().deserialize(if (target.isGlowing) enabledMessage else disabledMessage)
            )
        } catch (e: Exception) {
            this.stardustPlugin.getLogger().throwing(GlowCommand::class.java.simpleName, "handleGlow", e)
        }
    }
}