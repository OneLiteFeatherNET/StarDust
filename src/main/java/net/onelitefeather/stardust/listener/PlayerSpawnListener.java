package net.onelitefeather.stardust.listener;

import net.onelitefeather.stardust.position.SpawnManager;
import net.onelitefeather.stardust.position.SpawnPoint;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public record PlayerSpawnListener(SpawnManager spawnManager) implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handlePlayerSpawn(PlayerSpawnLocationEvent event) {

        Location origin = event.getSpawnLocation().clone();
        SpawnPoint spawnPoint = this.spawnManager.getSpawnPoint(origin.getWorld());

        if (spawnPoint != null) {
            if (spawnPoint.isDefaultSpawn()) {
                Location spawnLocation = spawnPoint.getWrappedLocation().toLocation();
                event.setSpawnLocation(spawnLocation != null && spawnPoint.hasPermission(event.getPlayer()) ? spawnLocation : origin);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handlePlayerRespawn(PlayerRespawnEvent event) {

        Location origin = event.getRespawnLocation().clone();
        SpawnPoint respawnPoint = this.spawnManager.getSpawnPoint(origin.getWorld());

        if (respawnPoint != null) {
            if (respawnPoint.isDefaultSpawn()) {
                Location respawnLocation = respawnPoint.getWrappedLocation().toLocation();
                event.setRespawnLocation(respawnLocation != null && respawnPoint.hasPermission(event.getPlayer()) ? respawnLocation : origin);
            }
        }
    }
}

