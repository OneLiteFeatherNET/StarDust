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


    String getName();

    void setName(String name);

    void setDisplayName(String displayName);

    String getDisplayName();

    void setFlying(boolean flying);

    boolean isFlying();

    void setVanished(boolean vanished);

    boolean isVanished();

    Player getBase();

    boolean toggleVanish();

    void hidePlayer();

    boolean kick(Component message);

    void checkCanFly();


}
