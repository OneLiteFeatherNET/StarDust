package net.onelitefeather.stardust.listener;

import com.destroystokyo.paper.event.entity.SkeletonHorseTrapEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.user.User;
import net.onelitefeather.stardust.user.UserPropertyType;
import org.bukkit.Bukkit;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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

    private boolean isVanished(Player player) {
        return stardustPlugin.getUserService().getVanishService().isVanished(player);
    }

    @EventHandler
    public void handleBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
        if (user == null) return;
        event.setCancelled(isVanished(player) && !user.isInBuildMode());
    }

    @EventHandler
    public void handleBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
        if (user == null) return;
        event.setCancelled(isVanished(player) && !user.isInBuildMode());
    }

    @EventHandler
    public void handleEntityDamage(EntityDamageEvent event) {
        if (possibleEntityAttackCauses.contains(event.getCause())) return;
        if (event.getEntity() instanceof Player player) {
            User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
            boolean isDamageAllowed = user != null && user.isPvPAllowed();
            event.setCancelled(isVanished(player) && !isDamageAllowed);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity target = event.getEntity();
        Entity attacker = event.getDamager();
        if (attacker instanceof Projectile) return;

        User attackerUser = stardustPlugin.getUserService().getUser(attacker.getUniqueId());
        if (attackerUser == null) return;
        User targetUser = stardustPlugin.getUserService().getUser(target.getUniqueId());

        boolean canAttack = attackerUser.isVanished() && attackerUser.isPvPAllowed();

        boolean cancel;
        if (attacker instanceof Player player && isVanished(player)) {
            cancel = !canAttack;
        } else if (targetUser != null && targetUser.isVanished()) {
            cancel = !canAttack;
        } else if (!(target instanceof Player)) {
            cancel = canAttack;
        } else if (target.isInvulnerable()) {
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

        User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
        if (user == null) return;

        event.setCancelled(user.isVanished() && !user.isPvPAllowed());
    }

    @EventHandler
    public void handleProjectileEntityHit(ProjectileHitEvent event) {

        if (event.getHitEntity() == null) return;
        Projectile projectile = event.getEntity();

        if (!(projectile.getShooter() instanceof Player shooter)) return;

        User user = stardustPlugin.getUserService().getUser(shooter.getUniqueId());
        if (user == null) return;

        event.setCancelled(user.isVanished() && !user.isPvPAllowed());
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        User user = null;
        Entity entity = null;
        if (event.getEntity() instanceof Player) {
            user = stardustPlugin.getUserService().getUser(event.getEntity().getUniqueId());
            entity = event.getEntity();
        } else if (event.getTarget() instanceof Player) {
            user = stardustPlugin.getUserService().getUser(event.getTarget().getUniqueId());
            entity = event.getTarget();
        }
        if ((user != null && user.isVanished()) || (entity != null && entity.isInvulnerable())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
        if (user == null) return;
        if (!user.isVanished()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getFoodLevel() >= event.getEntity().getFoodLevel()) return; //Ignore food level increment
        if (!player.isInvulnerable()) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            try {
                User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
                if (user == null) return;
                event.setCancelled(user.isVanished() && !user.isItemCollectDisabled());
            } catch (Exception e) {
                stardustPlugin.getLogger().throwing(PlayerVanishListener.class.getSimpleName(), "onPickUp", e);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        try {
            User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
            if (user == null) return;
            event.setCancelled(user.isVanished() && !user.isItemDropDisabled());
        } catch (Exception e) {
            stardustPlugin.getLogger().throwing(PlayerVanishListener.class.getSimpleName(), "onDrop", e);
        }
    }

    @EventHandler
    public void onPlayerPickupExp(PlayerPickupExperienceEvent event) {
        Player player = event.getPlayer();
        User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
        if (user == null) return;
        try {
            event.setCancelled(user.isVanished() && !user.isItemCollectDisabled());
        } catch (Exception e) {
            stardustPlugin.getLogger().throwing(PlayerVanishListener.class.getSimpleName(), "onPlayerPickupExp", e);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        try {
            User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
            if (user == null) return;
            if (user.isVanished()) {
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
        User user = stardustPlugin.getUserService().getUser(player.getUniqueId());
        if (user == null || !user.isVanished()) return;

        boolean isBuildingDenied = Boolean.FALSE.equals(
                user.getProperty(UserPropertyType.VANISH_ALLOW_BUILDING).getValue()
        );
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
        event.setCancelled(stardustPlugin.getUserService().getVanishService().isVanished(resultPlayer));
    }

    @EventHandler
    public void handleRaidTrigger(RaidTriggerEvent event) {
        Player player = event.getPlayer();
        event.setCancelled(isVanished(player));
    }

    @EventHandler
    public void handleRaidTrigger(SkeletonHorseTrapEvent event) {
        long notVanishedSize = event.getEligibleHumans().stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .filter(p -> !isVanished(p))
                .count();
        event.setCancelled(notVanishedSize == 0);
    }
}
