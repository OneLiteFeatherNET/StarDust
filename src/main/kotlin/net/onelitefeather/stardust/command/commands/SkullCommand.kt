package net.onelitefeather.stardust.command.commands

import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Permission

class SkullCommand(private val stardustPlugin: StardustPlugin) {

    @Command("skull [name]")
    @Permission("stardust.command.skull")
    fun handleCommand(player: Player, @Greedy @Argument(value = "name") name: String?) {

        try {
            val skullOwner = name ?: player.name

            stardustPlugin.server.asyncScheduler.runNow(stardustPlugin) {
                val skullItem = ItemStack(Material.PLAYER_HEAD)
                val skullMeta = skullItem.itemMeta as SkullMeta
                val skullOwnerId = stardustPlugin.server.getPlayerUniqueId(skullOwner) ?: player.uniqueId
                val offlinePlayer = stardustPlugin.server.getOfflinePlayer(skullOwnerId)
                if (!offlinePlayer.hasPlayedBefore()) {
                    skullMeta.playerProfile = stardustPlugin.server.createProfile(skullOwnerId)
                } else {
                    skullMeta.owningPlayer = offlinePlayer
                }

                skullItem.itemMeta = skullMeta
                player.inventory.addItem(skullItem)
            }

            player.sendMessage(
                Component.translatable("commands.skull.success").arguments(
                    stardustPlugin.getPluginPrefix(),
                    Component.text(skullOwner)
                )
            )

        } catch (e: Exception) {
            this.stardustPlugin.logger.throwing(SkullCommand::class.java.simpleName, "handleCommand", e)
        }
    }
}