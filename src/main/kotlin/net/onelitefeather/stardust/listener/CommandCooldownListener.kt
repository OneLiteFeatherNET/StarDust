package net.onelitefeather.stardust.listener

import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import kotlin.math.abs

class CommandCooldownListener(private val stardustPlugin: StardustPlugin) : Listener {

    @EventHandler
    fun handlePlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {

        val player = event.player
        try {

            val commandRaw = event.message.replaceFirst("/", "")
            val strings = commandRaw.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()

            if (strings.isEmpty()) return

            val commandLabelRaw = strings[0]
            val commandLabel = if (commandLabelRaw.contains(":")) commandLabelRaw.split(":")[1] else commandLabelRaw

            if (stardustPlugin.commandCooldownService.hasCommandCooldown(commandLabel)) {

                if (player.hasPermission("stardust.commandcooldown.bypass") && stardustPlugin.config.getBoolean("settings.use-cooldown-bypass")) return
                val commandCooldown =
                    stardustPlugin.commandCooldownService.getCommandCooldown(player.uniqueId, commandLabel)

                if (commandCooldown != null && !commandCooldown.isOver()) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:plugin.command-cooldowned:'${stardustPlugin.getPluginPrefix()}':${getRemainingTime(commandCooldown.executedAt)}"))
                    event.isCancelled = true
                    return
                }

                val cooldownData = stardustPlugin.commandCooldownService.getCooldownData(commandLabel)
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

    fun getRemainingTime(time: Long): String {
        val diff = abs(time - System.currentTimeMillis())
        val seconds = diff / 1000 % 60
        val minutes = diff / (1000 * 60) % 60
        val hours = diff / (1000 * 60 * 60) % 24
        val days = diff / (1000 * 60 * 60 * 24)
        val remainingTime = if (days > 0) {
            "<lang:remaining-time.days:$days:$hours:$minutes:$seconds>"
        } else if (hours > 0) {
            "<lang:remaining-time.hours:$hours:$minutes:$seconds>"
        } else if (minutes > 0) {
            "<lang:remaining-time.minutes:$minutes:$seconds>"
        } else {
            "<lang:remaining-time.seconds:$seconds>"
        }
        return remainingTime
    }
}