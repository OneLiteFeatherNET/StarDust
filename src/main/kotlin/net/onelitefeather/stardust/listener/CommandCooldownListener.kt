package net.onelitefeather.stardust.listener

import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.util.concurrent.TimeUnit

class CommandCooldownListener(private val stardustPlugin: StardustPlugin) : Listener {

    @EventHandler
    fun handlePlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {

        val commandRaw = event.message.replaceFirst("/".toRegex(), "")
        val strings = commandRaw.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val commandLabel = strings[0]

        val player = event.player
        if (strings.copyOfRange(1, strings.size).isNotEmpty() &&
            stardustPlugin.commandCooldownService.hasCommandCooldown(commandLabel) &&
            !player.hasPermission("essentials.commandcooldown.bypass")
        ) {

            val commandCooldown =
                stardustPlugin.commandCooldownService.getCommandCooldown(player.uniqueId, commandLabel)

            if (!player.hasPermission("featheressentials.commandcooldown.bypass")) {
                if (commandCooldown != null && !stardustPlugin.commandCooldownService.isCooldownOver(
                        player.uniqueId,
                        commandLabel
                    )
                ) {
                    player.sendMessage(
                        stardustPlugin.i18nService.getMessage(
                            "plugin.command-cooldowned",
                            stardustPlugin.i18nService.getPluginPrefix(),
                            stardustPlugin.i18nService.getRemainingTime(commandCooldown.executedAt)
                        )
                    )

                    event.isCancelled = true
                    return
                }

                val timeUnit =
                    TimeUnit.valueOf(stardustPlugin.config.getString("command-cooldowns.$commandLabel.timeunit")!!)
                val time = stardustPlugin.config.getLong("command-cooldowns.$commandLabel.time")
                stardustPlugin.commandCooldownService.addCommandCooldown(
                    player.uniqueId,
                    commandLabel,
                    timeUnit,
                    time,
                )
            }
        }
    }
}