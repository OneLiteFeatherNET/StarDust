package net.onelitefeather.stardust.listener

import com.destroystokyo.paper.event.entity.SkeletonHorseTrapEvent
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.user.User
import net.onelitefeather.stardust.user.UserPropertyType
import net.onelitefeather.stardust.util.PlayerUtils
import org.bukkit.Bukkit
import org.bukkit.block.data.Powerable
import org.bukkit.entity.*
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.BlockReceiveGameEvent
import org.bukkit.event.entity.*
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.raid.RaidTriggerEvent


class PlayerVanishListener(private val stardustPlugin: StardustPlugin) : Listener, PlayerUtils {

    private val vanishService = stardustPlugin.userService.playerVanishService
    private val possibleEntityAttackCauses = listOf(
        DamageCause.ENTITY_ATTACK,
        DamageCause.ENTITY_SWEEP_ATTACK,
        DamageCause.PROJECTILE
    )

    @EventHandler
    fun handleBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val user = stardustPlugin.userService.getUser(player.uniqueId) ?: return
        event.isCancelled = vanishService.isVanished(player) && !user.isBuildingAllowed()
    }

    @EventHandler
    fun handleBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val user = stardustPlugin.userService.getUser(player.uniqueId) ?: return
        event.isCancelled = vanishService.isVanished(player) && !user.isBuildingAllowed()
    }

    @EventHandler
    fun handleEntityDamage(event: EntityDamageEvent) {
        if (possibleEntityAttackCauses.contains(event.cause)) return
        if (event.entity is Player) {

            val player = event.entity as Player
            val user = stardustPlugin.userService.getUser(player.uniqueId)
            val isDamageAllowed = user?.isPvPAllowed() ?: false

            event.isCancelled = vanishService.isVanished(player) && !isDamageAllowed
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {

        val target = event.entity
        val attacker = event.damager
        if (attacker is Projectile) return

        val attackerUser = stardustPlugin.userService.getUser(attacker.uniqueId) ?: return
        val targetUser = stardustPlugin.userService.getUser(target.uniqueId)

        val canAttack = attackerUser.isVanished() && attackerUser.isPvPAllowed()

        event.isCancelled = when {
            attacker is Player && vanishService.isVanished(attacker) -> !canAttack
            targetUser != null && targetUser.isVanished() -> !canAttack
            target !is Player -> canAttack
            target.isInvulnerable -> !attacker.hasPermission("stardust.bypass.damage.invulnerable")
            else -> false
        }
    }

    @EventHandler
    fun handleProjectileLaunch(event: ProjectileLaunchEvent) {
        if (event.entity !is ThrownPotion) return
        val entity = event.entity
        if (entity.shooter !is Entity) return
        val shooter = entity.shooter as Entity
        val user = stardustPlugin.userService.getUser(shooter.uniqueId) ?: return
        event.isCancelled = user.isVanished() && !user.isPvPAllowed()
    }

    @EventHandler
    fun handleProjectileEntityHit(event: ProjectileHitEvent) {
        if (event.hitEntity == null) return
        val entity = event.entity
        if (entity.shooter !is Entity) return
        val shooter = entity.shooter as Entity
        val user = stardustPlugin.userService.getUser(shooter.uniqueId) ?: return
        event.isCancelled = user.isVanished() && !user.isPvPAllowed()
    }

    @EventHandler
    fun onTarget(event: EntityTargetEvent) {

        var user: User? = null
        var entity: Entity? = null

        if (event.entity is Player) {
            user = stardustPlugin.userService.getUser(event.entity.uniqueId)
            entity = event.entity
        } else if (event.target is Player) {
            user = stardustPlugin.userService.getUser((event.target as Player).uniqueId)
            entity = event.target
        }

        if (user != null && user.isVanished() || entity != null && entity.isInvulnerable) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        if (event.entity is Player) {
            val player = event.entity as Player
            try {
                val user = stardustPlugin.userService.getUser(player.uniqueId)
                event.isCancelled = user != null && (user.isVanished() || player.isInvulnerable)
            } catch (e: Exception) {
                this.stardustPlugin.logger
                    .throwing(PlayerVanishListener::class.java.simpleName, "onFoodLevelChange", e)
            }
        }
    }

    @EventHandler
    fun onPickUp(event: EntityPickupItemEvent) {
        if (event.entity is Player) {
            val player = event.entity as Player
            try {
                val user = stardustPlugin.userService.getUser(player.uniqueId) ?: return
                event.isCancelled = user.isVanished() && !user.isItemCollectDisabled()
            } catch (e: Exception) {
                this.stardustPlugin.logger
                    .throwing(PlayerVanishListener::class.java.simpleName, "onPickUp", e)
            }
        }
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val player = event.player
        try {
            val user = stardustPlugin.userService.getUser(player.uniqueId) ?: return
            event.isCancelled = user.isVanished() && !user.isItemDropDisabled()
        } catch (e: Exception) {
            this.stardustPlugin.logger
                .throwing(PlayerVanishListener::class.java.simpleName, "onDrop", e)
        }
    }

    @EventHandler
    fun onPlayerPickupExp(event: PlayerPickupExperienceEvent) {
        val player = event.player
        val user = stardustPlugin.userService.getUser(player.uniqueId) ?: return
        try {
            event.isCancelled = user.isVanished() && !user.isItemCollectDisabled()
        } catch (e: Exception) {
            this.stardustPlugin.logger
                .throwing(PlayerVanishListener::class.java.simpleName, "onPlayerPickupExp", e)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        try {
            val user = stardustPlugin.userService.getUser(player.uniqueId) ?: return
            if (user.isVanished()) {
                event.drops.clear()
                event.deathMessage(Component.empty())
                event.keepInventory = true
                event.keepLevel = true
                event.setShouldDropExperience(false)
                event.setShouldPlayDeathSound(false)
            }
        } catch (e: Exception) {
            this.stardustPlugin.logger
                .throwing(PlayerVanishListener::class.java.simpleName, "onPlayerDeath", e)
        }
    }

    @EventHandler
    fun handlePhysicalInteract(event: PlayerInteractEvent) {

        val player = event.player

        val block = event.clickedBlock ?: return
        val user = stardustPlugin.userService.getUser(player.uniqueId) ?: return
        if (!user.isVanished()) return
        if (user.getProperty(UserPropertyType.VANISH_ALLOW_BUILDING)?.getValue<Boolean>() == true) return

        val isBlockPowerable = block.blockData is Powerable

        if (event.action == Action.PHYSICAL) {
            val isBlockPhysical = stardustPlugin.pluginConfig.physicalBlocks().contains(block.type)

            event.isCancelled = if (isBlockPhysical || isBlockPowerable) {
                true
            } else {
                event.useInteractedBlock() == Event.Result.DENY
            }

            return
        }

        event.isCancelled = if (isBlockPowerable) {
            !player.isSneaking
        } else {
            event.useInteractedBlock() == Event.Result.DENY
        }
    }

    @EventHandler
    fun handleGameEvent(event: BlockReceiveGameEvent) {

        val player = if (event.entity is Player) {
            event.entity as Player
        } else if (event.entity is Item) {
            val thrower = (event.entity as Item).thrower
            if (thrower != null) Bukkit.getPlayer(thrower) else null
        } else {
            null
        }

        if (player == null) return
        event.isCancelled = stardustPlugin.userService.playerVanishService.isVanished(player)
    }

    @EventHandler
    fun handleRaidTrigger(event: RaidTriggerEvent) {
        val player = event.player
        event.isCancelled = vanishService.isVanished(player)
    }

    @EventHandler
    fun handleRaidTrigger(event: SkeletonHorseTrapEvent) {
        val notVanishedSize = event.eligibleHumans.filterIsInstance<Player>().filterNot(vanishService::isVanished).size
        event.isCancelled = notVanishedSize == 0
    }
}