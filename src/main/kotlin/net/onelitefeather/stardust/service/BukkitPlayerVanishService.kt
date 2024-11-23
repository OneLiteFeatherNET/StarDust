package net.onelitefeather.stardust.service

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.api.PlayerVanishService
import net.onelitefeather.stardust.user.UserPropertyType
import net.onelitefeather.stardust.util.PlayerUtils
import net.onelitefeather.stardust.util.RADIUS_REMOVE_ENEMIES
import org.bukkit.entity.Player

class BukkitPlayerVanishService(private val stardustPlugin: StardustPlugin, private val userService: UserService) :
    PlayerVanishService<Player>, PlayerUtils {

    private val vanishSeeOthersPermission = "stardust.vanish.others"

    override fun hidePlayer(player: Player) {
        stardustPlugin.server.scheduler.getMainThreadExecutor(stardustPlugin).execute {
            stardustPlugin.server.onlinePlayers.forEach { players ->
                if (!canSee(players, player)) {
                    players.hidePlayer(stardustPlugin, player)
                }
            }
        }
    }

    override fun showPlayer(player: Player) {
        stardustPlugin.server.scheduler.getMainThreadExecutor(stardustPlugin).execute {
            stardustPlugin.server.onlinePlayers.forEach { players ->
                players.showPlayer(stardustPlugin, player)
                players.sendMessage(Component.translatable("listener.join-message").arguments(player.displayName()))
            }
        }
    }

    override fun toggle(player: Player): Boolean {

        val user = userService.getUser(player.uniqueId) ?: return false
        val currentState = user.isVanished()

        if (currentState) {
            showPlayer(player)
            sendReappearedMessage(player)
            player.allowFlight = canEnterFlyMode(player)
        } else {
            hidePlayer(player)
            removeEnemies(player, RADIUS_REMOVE_ENEMIES)
            sendDisappearedMessage(player)
            player.allowFlight = true
        }

        setVanished(player, !currentState)
        return isVanished(player)
    }

    override fun isVanished(player: Player): Boolean {
        val user = userService.getUser(player.uniqueId) ?: return false
        return user.isVanished()
    }

    override fun setVanished(player: Player, vanished: Boolean) {
        val user = userService.getUser(player.uniqueId) ?: return
        userService.setUserProperty(user, UserPropertyType.VANISHED, vanished)
    }

    override fun handlePlayerJoin(player: Player): Boolean {

        player.server.onlinePlayers.filter(this::isVanished).forEach(this::hidePlayer)

        if(handleAutoVanish(player)) return true

        if (isVanished(player)) {

            if (!player.hasPermission("stardust.command.vanish")) {
                setVanished(player, false)
                showPlayer(player)
                player.allowFlight = !canEnterFlyMode(player)
                return true
            }

            player.sendMessage(Component.translatable("vanish.join.self").arguments(stardustPlugin.getPluginPrefix()))
            broadcastMessage(player, Component.translatable("vanish.join.silently").arguments(vanishDisplayName(player)))
            hidePlayer(player)
        }

        return false
    }

    override fun handlePlayerQuit(player: Player) {
        if (!isVanished(player)) return
        broadcastMessage(
            player,
            Component.translatable("vanish.quit.silently")
                .arguments(stardustPlugin.getPluginPrefix(), vanishDisplayName(player))
        )
    }

    private fun sendDisappearedMessage(player: Player) {
        player.sendMessage(Component.translatable("vanish.self.disappeared").arguments(stardustPlugin.getPluginPrefix()))
        broadcastMessage(player, Component.translatable("vanish.disappeared").arguments
            (stardustPlugin.getPluginPrefix(), vanishDisplayName(player)))

        player.server.onlinePlayers.filterNot { it.hasPermission(vanishSeeOthersPermission) }.forEach {
            it.sendMessage(Component.translatable("listener.quit-message").arguments(player.displayName()))
        }
    }

    private fun sendReappearedMessage(player: Player) {
        player.sendMessage(Component.translatable("vanish.self.reappeared").arguments(stardustPlugin.getPluginPrefix()))

        broadcastMessage(player, Component.translatable("vanish.reappeared").arguments(
            stardustPlugin.getPluginPrefix(), vanishDisplayName(player))
        )
    }

    private fun handleAutoVanish(player: Player): Boolean {
        if (player.hasPermission("stardust.vanish.auto")) {

            val displayName = vanishDisplayName(player)

            setVanished(player, true)
            hidePlayer(player)
            removeEnemies(player, RADIUS_REMOVE_ENEMIES)
            player.allowFlight = true
            player.sendMessage(Component.translatable("vanish.join.self").arguments(stardustPlugin.getPluginPrefix()))

            broadcastMessage(
                player, Component.translatable("vanish.join.silently").arguments(
                    stardustPlugin.getPluginPrefix(), displayName
                )
            )
            return true
        }

        return false
    }

    private fun broadcastMessage(player: Player, message: Component) {
        player.server.onlinePlayers.filterNot { player == it }.forEach {
            if (it.hasPermission(vanishSeeOthersPermission)) {
                it.sendMessage(message)
            }
        }
    }

    private fun vanishDisplayName(player: Player): Component {
        val team = player.scoreboard.getPlayerTeam(player) ?: return Component.text(player.name)
        return MiniMessage.miniMessage().deserialize(player.name).color(team.color())
    }

    private fun canSee(player: Player, target: Player): Boolean {
        if (!isGroupWeightBased()) return player.hasPermission(vanishSeeOthersPermission)
        val playerGroupPriority = stardustPlugin.luckPermsService.getGroupPriority(player)
        val targetGroupPriority = stardustPlugin.luckPermsService.getGroupPriority(target)
        return playerGroupPriority >= targetGroupPriority
    }

    private fun isGroupWeightBased() = stardustPlugin.config.getBoolean("group-weight-based-visibility", true)
}
