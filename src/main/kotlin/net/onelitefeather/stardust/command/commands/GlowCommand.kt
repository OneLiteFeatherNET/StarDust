package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.coloredDisplayName
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GlowCommand(private val stardustPlugin: StardustPlugin) {

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
                commandSender.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                        stardustPlugin.i18nService.getMessage(
                            "plugin.not-enough-permissions", stardustPlugin.i18nService.getPluginPrefix()
                        )
                    )
                )

                return
            }

            val enabledMessage = stardustPlugin.i18nService.getMessage(
                "commands.glow.enabled", *arrayOf(
                    stardustPlugin.i18nService.getPluginPrefix(), target.coloredDisplayName()
                )
            )

            val disabledMessage = stardustPlugin.i18nService.getMessage(
                "commands.glow.disabled", *arrayOf(
                    stardustPlugin.i18nService.getPluginPrefix(), target.coloredDisplayName()
                )
            )

            target.isGlowing = !target.isGlowing
            commandSender.sendMessage(
                MiniMessage.miniMessage().deserialize(if (target.isGlowing) enabledMessage else disabledMessage)
            )
        } catch (e: Exception) {
            this.stardustPlugin.getLogger().throwing(GlowCommand::class.java.simpleName, "handleGlow", e)
        }
    }
}