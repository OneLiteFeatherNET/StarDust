package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.miniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Mob
import org.bukkit.entity.Player

class GodmodeCommand(private val stardustPlugin: StardustPlugin) {

    @CommandMethod("godmode [player]")
    @CommandPermission("stardust.command.godmode")
    @CommandDescription("Makes a player invulnerable to everything")
    fun handleCommand(commandSender: CommandSender, @Greedy @Argument(value = "player") target: Player?) {
        handleInvulnerability(commandSender, target ?: commandSender as Player)
    }

    private fun handleInvulnerability(commandSender: CommandSender, target: Player) {

        if (target != commandSender && !commandSender.hasPermission("stardust.command.godmode.others")) {
            commandSender.sendMessage(miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "plugin.not-enough-permissions",
                    stardustPlugin.i18nService.getPluginPrefix()
                )
            })
            return
        }

        target.isInvulnerable = !target.isInvulnerable
        removeEnemies(target)

        val enabled =
            stardustPlugin.i18nService.getMessage(
                "commands.god-mode.enable",
                stardustPlugin.i18nService.getPluginPrefix(),
                stardustPlugin.i18nService.translateLegacyString(target.displayName())
            )
        val disabled =
            stardustPlugin.i18nService.getMessage(
                "commands.god-mode.disable",
                stardustPlugin.i18nService.getPluginPrefix(),
                stardustPlugin.i18nService.translateLegacyString(target.displayName())
            )

        target.sendMessage(miniMessage { if (target.isInvulnerable) enabled else disabled })
        if (commandSender != target) {
            commandSender.sendMessage(miniMessage { if (target.isInvulnerable) enabled else disabled })
        }
    }

    private fun removeEnemies(player: Player) {
        if (player.isInvulnerable) {
            player.location.getNearbyLivingEntities(32.0).forEach { livingEntity ->
                if (livingEntity is Mob) {
                    val target = livingEntity.target ?: return@forEach
                    if (target == player) {
                        livingEntity.target = null
                    }
                }
            }
        }
    }
}