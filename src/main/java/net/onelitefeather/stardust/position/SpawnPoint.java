package net.onelitefeather.stardust.position;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpawnPoint {

    private String name, permission;
    private WrappedLocation wrappedLocation;
    private boolean defaultSpawn;

    public SpawnPoint(String name, WrappedLocation wrappedLocation, String permission, boolean defaultSpawn) {
        this.name = name;
        this.permission = permission;
        this.wrappedLocation = wrappedLocation;
        this.defaultSpawn = defaultSpawn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasPermission(Player player) {
        return this.permission == null || this.permission.isEmpty() || player.hasPermission(permission);
    }

    @Nullable
    public String getPermission() {
        return permission;
    }

    public void setPermission(@Nullable String permission) {
        this.permission = permission;
    }

    @NotNull
    public WrappedLocation getWrappedLocation() {
        return wrappedLocation;
    }

    public void setWrappedLocation(WrappedLocation wrappedLocation) {
        this.wrappedLocation = wrappedLocation;
    }

    public boolean isDefaultSpawn() {
        return defaultSpawn;
    }

    public void setDefaultSpawn(boolean defaultSpawn) {
        this.defaultSpawn = defaultSpawn;
    }

    @Override
    public String toString() {
        return "SpawnPoint{" +
                "name='" + name + '\'' +
                ", permission='" + permission + '\'' +
                ", wrappedLocation=" + wrappedLocation +
                ", defaultSpawn=" + defaultSpawn +
                '}';
    }


}
