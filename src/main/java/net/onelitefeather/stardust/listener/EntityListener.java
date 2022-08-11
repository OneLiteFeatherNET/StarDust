package net.onelitefeather.stardust.listener;

import net.onelitefeather.stardust.FeatherEssentials;
import net.onelitefeather.stardust.api.user.IUser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public record EntityListener(FeatherEssentials featherEssentials) implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {

        Entity entity = event.getEntity();
        IUser user = this.featherEssentials.getUserManager().getUser(entity.getUniqueId());
        if (user != null && (user.isVanished() || entity.isInvulnerable())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        Entity attacker = event.getDamager();
        Entity target = event.getEntity();
        IUser targetUser = this.featherEssentials.getUserManager().getUser(target.getUniqueId());

        if (targetUser != null) {

            if (!attacker.hasPermission("featheressentials.bypass.damage.vanish")) {
                if (targetUser.isVanished()) {
                    event.setCancelled(true);
                }
            }

            if (!attacker.hasPermission("featheressentials.bypass.damage.invulnerable")) {
                if (target.isInvulnerable()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {

        IUser user = null;
        Entity entity = null;

        if (event.getEntity() instanceof Player) {
            user = this.featherEssentials.getUserManager().getUser(event.getEntity().getUniqueId());
            entity = event.getEntity();
        } else if (event.getTarget() instanceof Player) {
            user = this.featherEssentials.getUserManager().getUser(event.getTarget().getUniqueId());
            entity = event.getTarget();
        }

        if (user != null && user.isVanished() || entity != null && entity.isInvulnerable()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            IUser user = this.featherEssentials.getUserManager().getUser(player.getUniqueId());
            if (user != null && (user.isVanished() || player.isInvulnerable())) {
                event.setCancelled(true);
            }
        }
    }
}
