package net.onelitefeather.stardust.service

import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.api.PlayerVanishService
import net.onelitefeather.stardust.extenstions.removeEnemies
import net.onelitefeather.stardust.user.User
import net.onelitefeather.stardust.user.UserPropertyType
import org.bukkit.entity.Player

class BukkitPlayerVanishService(private val stardustPlugin: StardustPlugin, private val userService: UserService) :
    PlayerVanishService<Player> {

    override fun hidePlayer(player: Player) {
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
            player.removeEnemies(32.0)
        }

        setVanished(user, !currentState)
        return isVanished(player)
    }

    override fun isVanished(player: Player): Boolean {
        val user = userService.getUser(player.uniqueId) ?: return false
        val vanishedProperty = userService.getUserProperty(user.properties, UserPropertyType.VANISHED) ?: return false
        return vanishedProperty.getValue()!!
    }

    override fun setVanished(user: User, vanished: Boolean) {
        userService.setUserProperty(user, UserPropertyType.VANISHED, vanished)
    }

}