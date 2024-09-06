package net.onelitefeather.stardust.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslationArgument
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

            val labelOrCommand = strings[0]
            val command = if (labelOrCommand.contains(":")) labelOrCommand.substringAfter(':') else labelOrCommand

            if (stardustPlugin.commandCooldownService.hasCommandCooldown(command)) {

                if (player.hasPermission("stardust.commandcooldown.bypass") && stardustPlugin.config.getBoolean("settings.use-cooldown-bypass")) return
                val commandCooldown =
                    stardustPlugin.commandCooldownService.getCommandCooldown(player.uniqueId, command)

                if (commandCooldown != null && !commandCooldown.isOver()) {

                    player.sendMessage(Component.translatable("plugin.command-cooldowned").arguments(
                        stardustPlugin.getPluginPrefix(),
                        getRemainingTime(commandCooldown.executedAt)))

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
            this.stardustPlugin.logger
                .throwing(CommandCooldownListener::class.java.simpleName, "handlePlayerCommandPreprocess", e)
        }
    }

    fun getRemainingTime(time: Long): Component {
        val diff = abs(time - System.currentTimeMillis())
        val seconds = diff / 1000 % 60
        val minutes = diff / (1000 * 60) % 60
        val hours = diff / (1000 * 60 * 60) % 24
        val days = diff / (1000 * 60 * 60 * 24)
        val remainingTime = when {
            days > 0 -> {
                Component.translatable("remaining-time.days").arguments(
                    TranslationArgument.numeric(days),
                    TranslationArgument.numeric(hours),
                    TranslationArgument.numeric(minutes),
                    TranslationArgument.numeric(seconds))
            }
            hours > 0 -> {
                Component.translatable("remaining-time.hours").arguments(
                    TranslationArgument.numeric(hours),
                    TranslationArgument.numeric(minutes),
                    TranslationArgument.numeric(seconds))
            }
            minutes > 0 -> {
                Component.translatable("remaining-time.minutes").arguments(
                    TranslationArgument.numeric(minutes),
                    TranslationArgument.numeric(seconds))
            }
            else -> {
                Component.translatable("remaining-time.seconds").arguments(TranslationArgument.numeric(seconds))
            }
        }
        return remainingTime
    }
}