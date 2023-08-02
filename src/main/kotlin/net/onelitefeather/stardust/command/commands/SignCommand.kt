package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Quoted
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.translation.GlobalTranslator
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.util.DATE_FORMAT
import net.onelitefeather.stardust.util.PlayerUtils
import net.onelitefeather.stardust.util.StringUtils
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SignCommand(private val stardustPlugin: StardustPlugin) : StringUtils, PlayerUtils {

    @CommandMethod("unsign")
    @CommandPermission("stardust.command.unsign")
    @CommandDescription("Remove your signature from a Item")
    fun execute(player: Player) {

        val itemStack = player.inventory.itemInMainHand
        if (!stardustPlugin.itemSignService.hasSigned(itemStack, player)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:commands.unsign.not-signed:'${stardustPlugin.getPluginPrefix()}'>"))
            return
        }

        giveItemStack(player, stardustPlugin.itemSignService.removeSignature(itemStack, player))
        player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:commands.unsign.success:'${stardustPlugin.getPluginPrefix()}'>"))
    }

    @CommandMethod("sign <text>")
    @CommandPermission("stardust.command.sign")
    @CommandDescription("Signature the Item in your Hand.")
    fun handleCommand(player: Player, @Argument(value = "text") @Quoted text: String) {

        val itemStack = player.inventory.itemInMainHand
        if (itemStack.type == Material.AIR) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:commands.sign.no-item-in-hand:'${stardustPlugin.getPluginPrefix()}'>"))
            return
        }

        val signService = stardustPlugin.itemSignService
        if (signService.hasSigned(itemStack, player) && !player.hasPermission("stardust.command.sign.override")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:commands.sign.already-signed:'${stardustPlugin.getPluginPrefix()}'>"))
            return
        }

        val message = MiniMessage.miniMessage().deserialize("<lang:commands.sign.item-lore-message:'${colorText(text)}':'${coloredDisplayName(player)}':'${DATE_FORMAT.format(System.currentTimeMillis())}'>")

        giveItemStack(player, signService.sign(itemStack, listOf(GlobalTranslator.render(message, player.locale())), player))

        player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:commands.sign.signed:'${stardustPlugin.getPluginPrefix()}'>"))

    }

    private fun giveItemStack(player: Player, itemStack: ItemStack) {
        if (player.gameMode == GameMode.CREATIVE) {
            player.inventory.setItemInMainHand(itemStack)
        } else {
            if (player.inventory.firstEmpty() != -1) {
                player.inventory.setItem(player.inventory.firstEmpty(), itemStack)
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<lang:plugin.inventory-full:'${stardustPlugin.getPluginPrefix()}'>"))
            }
        }
    }
}