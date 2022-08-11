package net.onelitefeather.stardust.api.user;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface IUser {

    void setId(int id);

    int getId();

    UUID getUniqueId();

    void setUniqueId(UUID uniqueId);

    List<UUID> getIgnoredPlayers();

    void setIgnoredPlayers(List<UUID> ignoredPlayers);

    boolean isIgnoring(IUser other);

    boolean isIgnoring(UUID uuid);

    void ignorePlayer(UUID uuid);

    void unIgnorePlayer(UUID uuid);

    String getName();

    void setName(String name);

    void setDisplayName(String displayName);

    String getDisplayName();

    void setTeleportIgnoring(boolean teleportIgnoring);

    boolean isTeleportIgnoring();

    void setFlying(boolean flying);

    boolean isFlying();

    void setVanished(boolean vanished);

    boolean isVanished();

    Player getBase();

    void setBackLocation(Location backLocation);

    Location getBackLocation();

    void setFirstJoin(long firstJoin);

    long getFirstJoin();

    void setLastSeen(long lastSeen);

    long getLastSeen();

    boolean toggleVanish();

    void hidePlayer();

    boolean kick(Component message);

    void checkCanFly();


}
