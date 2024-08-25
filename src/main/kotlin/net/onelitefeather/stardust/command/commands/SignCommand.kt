package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Quoted
import net.kyori.adventure.text.Component
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
            player.sendMessage(Component.translatable("commands.unsign.not-signed").arguments(stardustPlugin.getPluginPrefix()))
            return
        }

        giveItemStack(player, stardustPlugin.itemSignService.removeSignature(itemStack, player))
        player.sendMessage(Component.translatable("commands.unsign.success").arguments(stardustPlugin.getPluginPrefix()))
    }

    @CommandMethod("sign <text>")
    @CommandPermission("stardust.command.sign")
    @CommandDescription("Signature the Item in your Hand.")
    fun handleCommand(player: Player, @Argument(value = "text") @Quoted text: String) {

        val itemStack = player.inventory.itemInMainHand
        if (itemStack.type == Material.AIR) {
            player.sendMessage(Component.translatable("commands.sign.no-item-in-hand").arguments(stardustPlugin.getPluginPrefix()))
            return
        }

        val signService = stardustPlugin.itemSignService
        if (signService.hasSigned(itemStack, player) && !player.hasPermission("stardust.command.sign.override")) {
            player.sendMessage(Component.translatable("commands.sign.already-signed").arguments(stardustPlugin.getPluginPrefix()))
            return
        }

        val coloredText = colorText(text)
        val formattedDate = DATE_FORMAT.format(System.currentTimeMillis())
        val message = Component.translatable("commands.sign.item-lore-message").arguments(coloredText, Component.text(formattedDate))

        giveItemStack(player, signService.sign(itemStack, listOf(message), player))
        player.sendMessage(Component.translatable("commands.sign.signed").arguments(stardustPlugin.getPluginPrefix()))
    }

    private fun giveItemStack(player: Player, itemStack: ItemStack) {
        if (player.gameMode == GameMode.CREATIVE) {
            player.inventory.setItemInMainHand(itemStack)
        } else {
            if (player.inventory.firstEmpty() != -1) {
                player.inventory.setItem(player.inventory.firstEmpty(), itemStack)
            } else {
                player.sendMessage(Component.translatable("plugin.inventory-full").arguments(stardustPlugin.getPluginPrefix()))
            }
        }
    }
}