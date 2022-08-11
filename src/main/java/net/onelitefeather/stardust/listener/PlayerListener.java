package net.onelitefeather.stardust.listener;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.onelitefeather.stardust.FeatherEssentials;
import net.onelitefeather.stardust.api.user.IUser;
import net.onelitefeather.stardust.command.CommandCooldown;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public record PlayerListener(FeatherEssentials featherEssentials) implements Listener {

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {

        String commandRaw = event.getMessage().replaceFirst("/", "");
        String[] strings = commandRaw.split(" ");
        String commandLabel = strings[0];

        SimpleCommandMap commandMap = (SimpleCommandMap) Bukkit.getCommandMap();
        Command pluginCommand = commandMap.getCommand(commandLabel);
        if (pluginCommand == null) return;

        Player player = event.getPlayer();
        if (Arrays.copyOfRange(strings, 1, strings.length).length > 0) {
            if (this.featherEssentials.getCommandCooldownManager().hasCommandCooldown(commandLabel)) {

                CommandCooldown commandCooldown = this.featherEssentials.getCommandCooldownManager().getCommandCooldown(player.getUniqueId(), commandLabel);
                if (!player.hasPermission("featheressentials.commandcooldown.bypass")) {

                    if (commandCooldown != null) {
                        if (!this.featherEssentials.getCommandCooldownManager().isCommandCooldownOver(player.getUniqueId(), commandLabel)) {
                            player.sendMessage(this.featherEssentials.getMessage("plugin.command-cooldowned", this.featherEssentials.getPrefix(), this.featherEssentials.getRemainingTime(commandCooldown.getExecutedAt())));
                            event.setCancelled(true);
                            return;
                        }
                    }

                    TimeUnit timeUnit = TimeUnit.valueOf(this.featherEssentials.getConfig().getString("command-cooldowns." + commandLabel + ".timeunit"));
                    long time = this.featherEssentials.getConfig().getLong("command-cooldowns." + commandLabel + ".time");
                    this.featherEssentials.getCommandCooldownManager().addCommandCooldown(player.getUniqueId(), commandLabel, timeUnit, time, player.hasPermission("essentials.commandcooldown.bypass"));
                }
            }
        }
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        event.viewers().forEach(audience -> {
            if (audience instanceof Player viewer) {
                IUser viewerUser = this.featherEssentials.getUserManager().getUser(viewer.getUniqueId());
                if (viewerUser != null) {
                    if (viewerUser.isIgnoring(player.getUniqueId())) {
                        event.viewers().remove(viewer);
                    }
                }
            }
        });

        event.renderer((source, sourceDisplayName, message, viewer) -> Component.text()
                .append(sourceDisplayName)
                .append(Component.text(": "))
                .append(source.hasPermission("chat.color") ? Component.text(ChatColor.translateAlternateColorCodes('&', LegacyComponentSerializer.legacySection().serialize(event.message().replaceText(builder -> builder.match("<3").replacement("§4❤"))))) : event.message().replaceText(builder -> builder.match("<3").replacement("§4❤")))
                .build());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        IUser user = this.featherEssentials.getUserManager().getUser(player.getUniqueId());
        if (!event.isCancelled()) {
            if (user != null) {
                user.setBackLocation(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        IUser user = this.featherEssentials.getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            user.setBackLocation(player.getLocation());
            if (user.isVanished()) {
                event.getDrops().clear();
                event.deathMessage(Component.text(""));
                event.setKeepInventory(true);
                event.setKeepLevel(true);
                event.setShouldDropExperience(false);
                event.setShouldPlayDeathSound(false);
            }
        }
    }

    @EventHandler
    public void onPlayerPickupExp(PlayerPickupExperienceEvent event) {

        Player player = event.getPlayer();
        IUser user = this.featherEssentials.getUserManager().getUser(player.getUniqueId());

        if (user != null) {
            if (user.isVanished()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent event) {
        LivingEntity livingEntity = event.getEntity();
        IUser user = this.featherEssentials.getUserManager().getUser(livingEntity.getUniqueId());
        if (livingEntity instanceof Player && user != null && user.isVanished())
            event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        IUser user = this.featherEssentials.getUserManager().getUser(player.getUniqueId());
        if (user != null && user.isVanished())
            event.setCancelled(true);
    }
}
