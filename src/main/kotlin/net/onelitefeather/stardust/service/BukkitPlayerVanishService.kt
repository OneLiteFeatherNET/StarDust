package net.onelitefeather.stardust.service

import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.api.PlayerVanishService
import net.onelitefeather.stardust.user.User
import net.onelitefeather.stardust.user.UserPropertyType
import net.onelitefeather.stardust.util.PlayerUtils
import net.onelitefeather.stardust.util.RADIUS_REMOVE_ENEMIES
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class BukkitPlayerVanishService(private val stardustPlugin: StardustPlugin, private val userService: UserService) :
    PlayerVanishService<Player>, PlayerUtils {

        private val vanishedMetadataKey: NamespacedKey = NamespacedKey(stardustPlugin, "vanished")

    override fun hidePlayer(player: Player) {

        setVanishedMetadata(player, true)
        val playerGroupPriority = stardustPlugin.luckPermsService.getGroupPriority(player)
        stardustPlugin.server.onlinePlayers.forEach { players ->

            if (stardustPlugin.luckPermsService.isEnabled()) {
                if (playerGroupPriority > stardustPlugin.luckPermsService.getGroupPriority(players)) {
                    players.hidePlayer(stardustPlugin, player)
                }
            } else {
                if (!players.hasPermission("stardust.bypass.vanish")) {
                    players.hidePlayer(stardustPlugin, player)
                }
            }
        }
    }

    override fun showPlayer(player: Player) {

        setVanishedMetadata(player, false)
        stardustPlugin.server.onlinePlayers.filterNot { it.canSee(player) }
            .forEach { it.showPlayer(stardustPlugin, player) }
    }

    override fun toggle(player: Player): Boolean {

        val user = userService.getUser(player.uniqueId) ?: return false
        val currentState = user.properties.isVanished()

        if (currentState) {
            showPlayer(player)
        } else {
            hidePlayer(player)
            removeEnemies(player, RADIUS_REMOVE_ENEMIES)
        }

        setVanished(user, !currentState)
        return isVanished(player)
    }

    override fun isVanished(player: Player): Boolean {

        if(player.persistentDataContainer.has(vanishedMetadataKey)) {
            return player.persistentDataContainer[vanishedMetadataKey, PersistentDataType.BOOLEAN] == true
        }

        val user = userService.getUser(player.uniqueId) ?: return false
        val vanishedProperty = userService.getUserProperty(user.properties, UserPropertyType.VANISHED)
        return vanishedProperty.getValue()!!
    }

    override fun setVanished(user: User, vanished: Boolean) {
        userService.setUserProperty(user, UserPropertyType.VANISHED, vanished)
    }

    override fun handlePlayerJoin(player: Player) {
        val user = userService.getUser(player.uniqueId) ?: return
        if (isVanished(player)) {

            if (!player.hasPermission("stardust.vanish.auto")) {
                setVanished(user, false)
                showPlayer(player)
                return
            }

            hidePlayer(player)
        }
    }

    private fun setVanishedMetadata(player: Player, vanished: Boolean) {
        player.persistentDataContainer[vanishedMetadataKey, PersistentDataType.BOOLEAN] = vanished
    }
}