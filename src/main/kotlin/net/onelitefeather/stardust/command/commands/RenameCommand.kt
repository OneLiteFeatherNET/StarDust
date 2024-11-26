package net.onelitefeather.stardust.command.commands

import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.StringUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.incendo.cloud.annotation.specifier.Quoted
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

class RenameCommand(private val stardustPlugin: StardustPlugin) : StringUtils {

    @Command("itemrename|rename <text>")
    @Permission("stardust.command.rename")
    @CommandDescription("Rename your current held item")
    fun handleCommand(player: Player, @Argument(value = "text") @Quoted text: String) {

        val itemInHand = player.inventory.itemInMainHand
        if (itemInHand.type == Material.AIR) {
            player.sendMessage(
                Component.translatable("commands.rename.invalid-item").arguments(stardustPlugin.getPluginPrefix())
            )
            return
        }

        val itemDisplayName = secureComponent(player, colorText(text))

        val itemMeta = itemInHand.itemMeta
        itemMeta.displayName(itemDisplayName)
        itemInHand.itemMeta = itemMeta
        player.updateInventory()
        player.sendMessage(
            Component.translatable("commands.rename.success").arguments(
                stardustPlugin.getPluginPrefix(),
                secureComponent(player, itemDisplayName)
            )
        )
    }
}