package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Quoted
import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.miniMessage
import net.onelitefeather.stardust.service.BukkitItemSignService
import net.onelitefeather.stardust.util.DATE_FORMAT
import org.bukkit.Material
import org.bukkit.entity.Player

class SignCommand(private val stardustPlugin: StardustPlugin) {

    @CommandMethod("sign <text>")
    @CommandPermission("stardust.command.sign")
    @CommandDescription("Sign the Item in your Hand.")
    fun handleCommand(player: Player, @Argument(value = "text") @Quoted text: String) {

        val itemStack = player.inventory.itemInMainHand
        if (itemStack.type == Material.AIR) {
            player.sendMessage(miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "commands.sign.no-item-in-hand",
                    *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
                )
            })
            return
        }

        val signService = BukkitItemSignService(stardustPlugin, itemStack)
        if (signService.isSigned() && !player.hasPermission("stardust.command.sign.override")) {
            player.sendMessage(miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "commands.sign.already-signed",
                    *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
                )
            })

            return
        }

        if (player.inventory.firstEmpty() == -1) {
            player.sendMessage(miniMessage {
                stardustPlugin.i18nService.getMessage(
                    "plugin.inventory-full",
                    *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
                )
            })
            return
        }

        val coloredText = stardustPlugin.i18nService.translateLegacyString(Component.text(text))
        val headerAndFooter = miniMessage { stardustPlugin.i18nService.getMessage("commands.sign.item-lore-header-footer") }
        val message = miniMessage {  stardustPlugin.i18nService.getMessage("commands.sign.item-lore-message", *arrayOf(coloredText)) }
        val author = miniMessage { stardustPlugin.i18nService.getMessage(
            "commands.sign.item-lore-author",
            *arrayOf(
                stardustPlugin.i18nService.translateLegacyString(player.displayName()),
                DATE_FORMAT.format(System.currentTimeMillis())
            )
        ) }

        player.inventory.setItemInMainHand(signService.sign(listOf(headerAndFooter, message, author, headerAndFooter), player))
        player.sendMessage(miniMessage {
            stardustPlugin.i18nService.getMessage(
                "commands.sign.signed",
                *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
            )
        })
    }
}