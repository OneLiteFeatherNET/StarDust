package net.onelitefeather.stardust.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

public class TeleportCountdown implements Runnable{

    private final FeatherEssentials featherEssentials;
    private int count;
    private final Player player;
    private final double health;

    private final Location startLocation, destination;
    protected int taskId;

    private final Component message;


    public TeleportCountdown(@NotNull FeatherEssentials featherEssentials, int count, @NotNull Player player, @NotNull Location startLocation, @NotNull Location destination, Component message) {
        this.featherEssentials = featherEssentials;
        this.count = count;
        this.player = player;
        this.destination = destination;
        this.health = player.getHealth();
        this.startLocation = startLocation;
        this.message = message;
        this.taskId = player.getServer().getScheduler().runTaskTimerAsynchronously(this.featherEssentials, this, 0L, 20).getTaskId();
    }

    public int getCount() {
        return count;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getDestination() {
        return destination;
    }

    private void stop(@NotNull Component message) {
        this.player.sendMessage(message);
        this.player.playSound(this.player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
        cancelTimer();
    }

    @Override
    public void run() {

        if (this.player == null) {
            cancelTimer();
            return;
        }

        if (this.player.getHealth() < this.health && this.player.getLastDamageCause() != null) {
            //stop(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("teleport-countdown.damage")));
            return;
        }

        if (hasMoved(player)) {
            //stop(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("teleport-countdown.moved")));
            return;
        }

        if (count != 0) {

            this.player.sendActionBar(Component.text(String.format("Teleport in %s " + (this.count == 1 ? "second" : "seconds"), this.count)));
            this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0F, 1.0F);

        } else {

            this.player.playSound(this.player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            Bukkit.getScheduler().runTask(this.featherEssentials, () -> {
                player.teleport(this.destination);
                player.sendActionBar(message);
                this.player.playSound(this.player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 1.0F, 1.0F);
            });

            cancelTimer();
        }

        count--;
    }

    public boolean hasMoved(@NotNull Player player) {
        Location currentLocation = player.getLocation();
        return NumberConversions.square(startLocation.getX() - currentLocation.getX()) + NumberConversions.square(startLocation.getZ() - currentLocation.getZ()) > 0.1;
    }

    private void cancelTimer() {
        player.getServer().getScheduler().cancelTask(this.taskId);
    }

}
