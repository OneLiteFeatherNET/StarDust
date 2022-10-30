package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.miniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class SkullCommand(private val stardustPlugin: StardustPlugin) {

    @CommandMethod("skull [name]")
    @CommandPermission("stardust.command.skull")
    fun handleCommand(player: Player, @Greedy @Argument(value = "name") name: String?) {

        val skullOwner = name ?: player.name

        val skullItem = ItemStack(Material.PLAYER_HEAD)
        val skullMeta = skullItem.itemMeta as SkullMeta

        val skullOwnerId = player.server.getPlayerUniqueId(skullOwner) ?: player.uniqueId
        val offlinePlayer = player.server.getOfflinePlayer(skullOwnerId)

        if (!offlinePlayer.hasPlayedBefore()) {
            skullMeta.playerProfile = player.server.createProfile(skullOwnerId, skullOwner)
        } else {
            skullMeta.owningPlayer = offlinePlayer
        }

        skullItem.itemMeta = skullMeta
        player.inventory.addItem(skullItem)

        player.sendMessage(miniMessage {
            stardustPlugin.i18nService.getMessage(
                "commands.skull.success",
                *arrayOf(stardustPlugin.i18nService.getPluginPrefix(), skullOwner)
            )
        })
    }
}