package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Quoted
import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.StringUtils
import org.bukkit.Material
import org.bukkit.entity.Player

class RenameCommand(private val stardustPlugin: StardustPlugin) : StringUtils {

    @CommandMethod("itemrename|rename <text>")
    @CommandPermission("stardust.command.rename")
    @CommandDescription("Rename a Item")
    fun handleCommand(player: Player, @Argument(value = "text") @Quoted text: String) {

        val itemInHand = player.inventory.itemInMainHand
        if (itemInHand.type == Material.AIR) {
            player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                    stardustPlugin.i18nService.getMessage(
                        "commands.rename.invalid-item",
                        *arrayOf(stardustPlugin.i18nService.getPluginPrefix())
                    )
                )
            )

            return
        }

        val itemMeta = itemInHand.itemMeta
        itemMeta.displayName(MiniMessage.miniMessage().deserialize(colorText(text)))
        itemInHand.itemMeta = itemMeta
        player.updateInventory()
        player.sendMessage(
            MiniMessage.miniMessage().deserialize(
                stardustPlugin.i18nService.getMessage(
                    "commands.rename.success",
                    *arrayOf(
                        stardustPlugin.i18nService.getPluginPrefix(),
                        colorText(text)
                    )
                )
            )
        )
    }
}