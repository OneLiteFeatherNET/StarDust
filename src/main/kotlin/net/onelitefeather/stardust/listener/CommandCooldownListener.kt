package net.onelitefeather.stardust.listener

import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class CommandCooldownListener(private val stardustPlugin: StardustPlugin) : Listener {

    @EventHandler
    fun handlePlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {

        val player = event.player
        try {

            val commandRaw = event.message.replaceFirst("/", "")
            val strings = commandRaw.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()

            if (strings.isEmpty()) return

            val labelOrCommand = strings[0]
            val command = if (labelOrCommand.contains(":")) labelOrCommand.substringAfter(':') else labelOrCommand

            if (stardustPlugin.commandCooldownService.hasCommandCooldown(command)) {

                if (player.hasPermission("stardust.commandcooldown.bypass") && stardustPlugin.config.getBoolean("settings.use-cooldown-bypass")) return
                val commandCooldown =
                    stardustPlugin.commandCooldownService.getCommandCooldown(player.uniqueId, command)

                if (commandCooldown != null && !commandCooldown.isOver()) {
                    player.sendMessage(
                        MiniMessage.miniMessage().deserialize(
                            stardustPlugin.i18nService.getMessage(
                                "plugin.command-cooldowned",
                                stardustPlugin.i18nService.getPluginPrefix(),
                                stardustPlugin.i18nService.getRemainingTime(commandCooldown.executedAt)
                            )
                        )
                    )

                    event.isCancelled = true
                    return
                }

                val cooldownData = stardustPlugin.commandCooldownService.getCooldownData(command)
                if (cooldownData != null) {
                    stardustPlugin.commandCooldownService.addCommandCooldown(
                        player.uniqueId,
                        cooldownData.commandName,
                        cooldownData.timeUnit,
                        cooldownData.time
                    )
                }
            }
        } catch (e: Exception) {
            this.stardustPlugin.getLogger()
                .throwing(CommandCooldownListener::class.java.simpleName, "handlePlayerCommandPreprocess", e)
        }
    }
}