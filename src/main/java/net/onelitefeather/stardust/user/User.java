package net.onelitefeather.stardust.user;

import jakarta.persistence.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.onelitefeather.stardust.api.user.IUser;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User implements IUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String uuid;

    @Column
    private String name;

    @Column
    private boolean vanished;

    @Column
    private boolean teleportIgnoring;

    @Column
    private boolean flying;

    @Column
    private long firstJoin;

    @Column
    private long lastSeen;

    private transient Location backLocation;

    @ElementCollection(fetch = FetchType.EAGER, targetClass = String.class)
    private List<String> ignoredPlayers;

    public User() {

    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public UUID getUniqueId() {
        return UUID.fromString(this.uuid);
    }

    @Override
    public void setUniqueId(UUID uniqueId) {
        this.uuid = uuid.toString();
    }

    @Override
    public List<UUID> getIgnoredPlayers() {
        //maybe other method as streams?
        return this.ignoredPlayers.stream().map(UUID::fromString).toList();
    }

    @Override
    public void setIgnoredPlayers(List<UUID> ignoredPlayers) {
        this.ignoredPlayers = ignoredPlayers.stream().map(UUID::toString).toList();
    }

    @Override
    public boolean isIgnoring(IUser other) {
        return isIgnoring(other.getUniqueId());
    }

    @Override
    public boolean isIgnoring(UUID uuid) {
        return this.ignoredPlayers.contains(uuid.toString());
    }

    @Override
    public void ignorePlayer(UUID uuid) {
        this.ignoredPlayers.add(uuid.toString());
    }

    @Override
    public void unIgnorePlayer(UUID uuid) {
        this.ignoredPlayers.remove(uuid.toString());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setDisplayName(String displayName) {
        Player player = getBase();
        if (player != null) {
            player.displayName(LegacyComponentSerializer.legacy('&').deserialize(displayName));
        }
    }

    @Override
    public String getDisplayName() {
        Player player = getBase();
        return player != null ? LegacyComponentSerializer.legacySection().serialize(player.displayName()) : getName();
    }

    @Override
    public void setTeleportIgnoring(boolean teleportIgnoring) {
        this.teleportIgnoring = teleportIgnoring;
    }

    @Override
    public boolean isTeleportIgnoring() {
        return this.teleportIgnoring;
    }

    @Override
    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    @Override
    public boolean isFlying() {
        return this.flying;
    }

    @Override
    public void setVanished(boolean vanished) {
        this.vanished = vanished;

        //Missing featherlibrary?
        if (Bukkit.getPluginManager().isPluginEnabled("FeatherLibrary")) {

        }
        Player base = getBase();
        if (base != null) {
            base.setSleepingIgnored(vanished);
            base.setMetadata("vanished", new FixedMetadataValue(FeatherEssentials.getInstance(), this.vanished));
        }
    }

    @Override
    public boolean isVanished() {
        return this.vanished;
    }

    @Override
    public Player getBase() {
        return Bukkit.getPlayer(getUniqueId());
    }

    @Override
    public void setBackLocation(Location backLocation) {
        this.backLocation = backLocation;
    }

    @Override
    public Location getBackLocation() {
        return this.backLocation;
    }

    @Override
    public void setFirstJoin(long firstJoin) {
        this.firstJoin = firstJoin;
    }

    @Override
    public long getFirstJoin() {
        return this.firstJoin;
    }

    @Override
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public long getLastSeen() {
        return this.lastSeen;
    }

    @Override
    public String toString() {
        return "User{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + getDisplayName() + '\'' +
                '}';
    }

    @Override
    public boolean toggleVanish() {
        if (!isVanished()) {
            setVanished(true);
            hidePlayer();
        } else {
            setVanished(false);
            Bukkit.getOnlinePlayers().forEach(player -> player.showPlayer(FeatherEssentials.getInstance(), getBase()));
        }

        getBase().setGlowing(isVanished());
        return this.vanished;
    }

    @Override
    public void hidePlayer() {
        FeatherEssentials featherEssentials = FeatherEssentials.getInstance();
        Player player = getBase();
        Bukkit.getOnlinePlayers().forEach(players -> {
            if (featherEssentials.getVaultHook().getGroupPriority(player.getUniqueId()) > featherEssentials.getVaultHook().getGroupPriority(players.getUniqueId())) {
                players.hidePlayer(FeatherEssentials.getInstance(), player);
            }
        });
    }

    @Override
    public boolean kick(Component message) {
        Player player = getBase();
        boolean online = player != null;
        if (online)
            player.kick(message);
        return online;
    }

    @Override
    public void checkCanFly() {
        Player player = getBase();
        if (player != null) {
            if (player.hasPermission("featheressentials.join.flight")) {
                if (this.isFlying()) {
                    if (!player.getAllowFlight())
                        player.setAllowFlight(true);
                }
            } else {
                if (player.getAllowFlight()) {
                    this.setFlying(false);
                    player.setAllowFlight(false);
                }
            }
        }
    }
}
