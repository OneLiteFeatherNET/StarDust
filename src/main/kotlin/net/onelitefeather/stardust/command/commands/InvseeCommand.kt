package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.*
import io.sentry.Sentry
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.addClient
import net.onelitefeather.stardust.extenstions.miniMessage
import net.onelitefeather.stardust.extenstions.toSentryUser
import org.bukkit.entity.Player

class InvseeCommand(private val stardustPlugin: StardustPlugin) {

    @CommandMethod("invsee <player>")
    @CommandPermission("stardust.command.invsee")
    @CommandDescription("Allow access to an Inventory from a Player.")
    fun onCommand(
        player: Player,
        @Argument(value = "player") target: Player,
        @Flag(value = "enderchest") enderchest: Boolean?
    ) {

        if (enderchest == true) {
            handleEnderChest(player, target)
            return
        }

        player.openInventory(target.inventory)
    }

    private fun handleEnderChest(player: Player, target: Player) {

        try {
            val permissionMessage = stardustPlugin.i18nService.getMessage(
                "plugin.not-enough-permissions",
                *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
            )

            if (!player.hasPermission("stardust.command.enderchest")) {
                player.sendMessage(miniMessage { permissionMessage })
                return
            }

            if (target == player) {
                player.openInventory(player.enderChest)
            } else {

                if (player != target && !player.hasPermission("stardust.command.enderchest.others")) {
                    player.sendMessage(miniMessage { permissionMessage })
                    return
                }

                player.openInventory(target.enderChest)
            }
        } catch (e: Exception) {
            Sentry.captureException(e) {
                it.user = target.toSentryUser()
                target.addClient(it)
            }
        }
    }
}