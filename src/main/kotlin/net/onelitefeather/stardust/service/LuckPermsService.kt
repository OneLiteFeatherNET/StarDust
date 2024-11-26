package net.onelitefeather.stardust.service

import net.luckperms.api.LuckPerms
import net.luckperms.api.event.EventSubscription
import net.luckperms.api.event.user.track.UserDemoteEvent
import net.luckperms.api.model.group.Group
import net.onelitefeather.stardust.StardustPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.logging.Level


class LuckPermsService(val stardustPlugin: StardustPlugin) {

    lateinit var luckPerms: LuckPerms
    private val luckPermsEvents: MutableList<EventSubscription<*>> = arrayListOf()

    fun init() {
        try {
            val server = stardustPlugin.server
            if (server.pluginManager.isPluginEnabled("LuckPerms")) {
                val provider = server.servicesManager.getRegistration(LuckPerms::class.java)
                if (provider != null) {
                    luckPerms = provider.provider
                    stardustPlugin.logger.log(Level.INFO, "Using ${provider.plugin.name} as Permission provider.")
                    subscribeToEvents()
                }
            }
        } catch (e: Exception) {
            stardustPlugin.logger.log(Level.SEVERE, "Cannot hook into luckperms", e)
        }
    }

    private fun subscribeToEvents() {
        luckPermsEvents.add(luckPerms.eventBus.subscribe(UserDemoteEvent::class.java) { event ->

            val player = Bukkit.getPlayer(event.user.uniqueId) ?: return@subscribe
            if(!stardustPlugin.userService.playerVanishService.isVanished(player)) return@subscribe
            stardustPlugin.userService.playerVanishService.toggle(player)
        })
    }

    fun unsubscribeEvents() {
        luckPermsEvents.forEach(EventSubscription<*>::close)
        luckPermsEvents.clear()
    }

    fun isEnabled(): Boolean = this::luckPerms.isInitialized

    fun getPrimaryGroup(player: Player): Group {
        val user = luckPerms.userManager.getUser(player.uniqueId) ?: return getDefaultGroup()
        return luckPerms.groupManager.getGroup(user.primaryGroup) ?: getDefaultGroup()
    }

    fun getGroupPriority(player: Player): Int {
        if (!isEnabled()) return 0
        return getPrimaryGroup(player).weight.orElse(0)
    }

    fun getDefaultGroup(): Group = luckPerms.groupManager.getGroup("default")!!
}