package net.onelitefeather.stardust.listener;

import com.destroystokyo.paper.event.entity.SkeletonHorseTrapEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.player.StardustPlayer;
import org.bukkit.Bukkit;
import org.bukkit.block.data.Powerable;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockReceiveGameEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.raid.RaidTriggerEvent;

import java.util.Arrays;
import java.util.List;

public class PlayerVanishListener implements Listener {

    private final StardustPlugin stardustPlugin;
    private final List<DamageCause> possibleEntityAttackCauses = Arrays.asList(
            DamageCause.ENTITY_ATTACK,
            DamageCause.ENTITY_SWEEP_ATTACK,
            DamageCause.PROJECTILE
    );

    public PlayerVanishListener(StardustPlugin stardustPlugin) {
        this.stardustPlugin = stardustPlugin;
    }

    @EventHandler
    public void handleBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        StardustPlayer stardustPlayer = stardustPlugin.getPlayerService().findById(player.getUniqueId());
        if (stardustPlayer == null) return;
        event.setCancelled(stardustPlayer.isVanished() && !stardustPlayer.canBuild());
    }

    @EventHandler
    public void handleBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        StardustPlayer stardustPlayer = stardustPlugin.getPlayerService().findById(player.getUniqueId());
        if (stardustPlayer == null) return;
        event.setCancelled(stardustPlayer.isVanished() && !stardustPlayer.canBuild());
    }

    @EventHandler
    public void handleEntityDamage(EntityDamageEvent event) {
        if (possibleEntityAttackCauses.contains(event.getCause())) return;
        if (event.getEntity() instanceof Player player) {
            var invulnerableToDamageSrc = player.isInvulnerableTo(event.getDamageSource());
            var vanished = this.stardustPlugin.getVanishService().isVanished(player.getUniqueId());
            event.setCancelled(invulnerableToDamageSrc && vanished);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        Entity attacker = event.getDamager();
        if (attacker instanceof TNTPrimed) return;
        if (attacker instanceof Projectile) return;

        StardustPlayer attackerPlayer = stardustPlugin.getPlayerService().findById(attacker.getUniqueId());
        if (attackerPlayer == null) return;
        StardustPlayer targetUser = stardustPlugin.getPlayerService().findById(target.getUniqueId());

        boolean canAttack = attackerPlayer.isVanished() && attackerPlayer.canAttack();

        boolean cancel;
        if (attacker instanceof Player && attackerPlayer.isVanished()) {
            cancel = !canAttack;
        } else if (targetUser != null && targetUser.isVanished()) {
            cancel = !canAttack;
        } else if (!(target instanceof Player)) {
            cancel = canAttack;
        } else if (target.isInvulnerableTo(event.getDamageSource())) {
            cancel = !attacker.hasPermission("stardust.bypass.damage.invulnerable");
        } else {
            cancel = false;
        }
        event.setCancelled(cancel);

    }

    @EventHandler
    public void handleProjectileLaunch(ProjectileLaunchEvent event) {

        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player player)) return;

        StardustPlayer stardustPlayer = stardustPlugin.getPlayerService().findById(player.getUniqueId());
        if (stardustPlayer == null) return;

        event.setCancelled(stardustPlayer.isVanished() && !stardustPlayer.canAttack());
    }

    @EventHandler
    public void handleProjectileEntityHit(ProjectileHitEvent event) {

        if (event.getHitEntity() == null) return;
        Projectile projectile = event.getEntity();

        if (!(projectile.getShooter() instanceof Player shooter)) return;

        StardustPlayer stardustPlayer = stardustPlugin.getPlayerService().findById(shooter.getUniqueId());
        if (stardustPlayer == null) return;

        event.setCancelled(stardustPlayer.isVanished() && !stardustPlayer.canAttack());
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        var stardustPlayer = stardustPlugin.getPlayerService().findById(player.getUniqueId());
        if (!stardustPlayer.isVanished()) return;
        var cannotAttack = !stardustPlayer.canAttack();
        event.setCancelled(cannotAttack || player.isMarkedInvulnerable());
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        StardustPlayer stardustPlayer = stardustPlugin.getPlayerService().findById(player.getUniqueId());
        event.setCancelled(stardustPlayer != null && stardustPlayer.isVanished());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getFoodLevel() >= event.getEntity().getFoodLevel()) return; //Ignore food level increment
        if (!player.isInvulnerableTo(DamageSource.builder(DamageType.STARVE).build())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            try {
                StardustPlayer stardustPlayer = stardustPlugin.getPlayerService().findById(player.getUniqueId());
                if (stardustPlayer == null) return;
                event.setCancelled(stardustPlayer.isVanished() && stardustPlayer.keepInv());
            } catch (Exception e) {
                stardustPlugin.getLogger().throwing(PlayerVanishListener.class.getSimpleName(), "onPickUp", e);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        try {
            Player player = event.getPlayer();
            StardustPlayer stardustPlayer = stardustPlugin.getPlayerService().findById(player.getUniqueId());
            if (stardustPlayer == null) return;
            event.setCancelled(stardustPlayer.isVanished() && stardustPlayer.keepInv());
        } catch (Exception e) {
            stardustPlugin.getLogger().throwing(PlayerVanishListener.class.getSimpleName(), "onDrop", e);
        }
    }

    @EventHandler
    public void onPlayerPickupExp(PlayerPickupExperienceEvent event) {
        try {
            Player player = event.getPlayer();
            StardustPlayer stardustPlayer = stardustPlugin.getPlayerService().findById(player.getUniqueId());
            if (stardustPlayer == null) return;
            event.setCancelled(stardustPlayer.isVanished() && stardustPlayer.keepInv());
        } catch (Exception e) {
            stardustPlugin.getLogger().throwing(PlayerVanishListener.class.getSimpleName(), "onPlayerPickupExp", e);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        try {
            StardustPlayer stardustPlayer = stardustPlugin.getPlayerService().findById(player.getUniqueId());
            if (stardustPlayer == null) return;
            if (stardustPlayer.isVanished()) {
                event.getDrops().clear();
                event.deathMessage(Component.empty());
                event.setKeepInventory(true);
                event.setKeepLevel(true);
                event.setShouldDropExperience(false);
                event.setShouldPlayDeathSound(false);
            }
        } catch (Exception e) {
            stardustPlugin.getLogger().throwing(PlayerVanishListener.class.getSimpleName(), "onPlayerDeath", e);
        }
    }

    @EventHandler
    public void handlePhysicalInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return;
        StardustPlayer stardustPlayer = stardustPlugin.getPlayerService().findById(player.getUniqueId());
        if (stardustPlayer == null || !stardustPlayer.isVanished()) return;

        boolean isBuildingDenied = stardustPlayer.isVanished() && !stardustPlayer.canBuild();
        boolean isBlockPowerable = event.getClickedBlock().getBlockData() instanceof Powerable;

        if (event.getAction() == Action.PHYSICAL) {
            boolean isBlockPhysical = stardustPlugin.getPluginConfiguration().isPhysicalBlock(event.getClickedBlock().getType());
            event.setCancelled((isBlockPhysical || isBlockPowerable) ? isBuildingDenied : event.useInteractedBlock() == Event.Result.DENY);
            return;
        }
        event.setCancelled(isBlockPowerable ? isBuildingDenied : event.useInteractedBlock() == Event.Result.DENY);
    }

    @EventHandler
    public void handleGameEvent(BlockReceiveGameEvent event) {
        Player resultPlayer = null;
        if (event.getEntity() instanceof Player player) {
            resultPlayer = player;
        } else if (event.getEntity() instanceof Item item && item.getThrower() != null) {
            resultPlayer = Bukkit.getPlayer(item.getThrower());
        }

        if (resultPlayer == null) return;
        event.setCancelled(stardustPlugin.getVanishService().isVanished(resultPlayer.getUniqueId()));
    }

    @EventHandler
    public void handleRaidTrigger(RaidTriggerEvent event) {
        StardustPlayer stardustPlayer = stardustPlugin.getPlayerService().findById(event.getPlayer().getUniqueId());
        if (stardustPlayer == null) return;
        event.setCancelled(stardustPlayer.isVanished());
    }

    @EventHandler
    public void handleRaidTrigger(SkeletonHorseTrapEvent event) {
        long notVanishedSize = event.getEligibleHumans().stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .filter(p -> !this.stardustPlugin.getVanishService().isVanished(p.getUniqueId()))
                .count();
        event.setCancelled(notVanishedSize == 0);
    }
}
