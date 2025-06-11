package net.onelitefeather.stardust.user;

import jakarta.persistence.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Entity
@Table
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @Column
    private final String uuid;

    @Column
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<UserProperty> properties;

    public User(@Nullable Long id) {
        this(id, UUID.randomUUID(), "null", null);
    }

    public User(Long id, UUID uuid, String name, List<UserProperty> properties) {
        this.id = id;
        this.uuid = uuid.toString();
        this.name = name;
        this.properties = properties;
    }

    public Long getId() {
        return id;
    }

    public UUID getUniqueId() {
        return UUID.fromString(uuid);
    }

    public String getName() {
        return name;
    }

    public User withName(String name) {
        this.name = name;
        return this;
    }

    public UserProperty getProperty(UserPropertyType type) {
        return properties.stream().filter(property -> property.getName().equals(type.getName())).findFirst().orElse(null);
    }

    public List<UserProperty> getProperties() {
        return properties;
    }

    public boolean isInBuildMode() {
        return getPropertyState(UserPropertyType.VANISH_ALLOW_BUILDING);
    }

    public boolean isPvPAllowed() {
        return getPropertyState(UserPropertyType.VANISH_ALLOW_PVP);
    }

    public boolean isItemCollectDisabled() {
        return getPropertyState(UserPropertyType.VANISH_DISABLE_ITEM_COLLECT);
    }

    public boolean isItemDropDisabled() {
        return getPropertyState(UserPropertyType.VANISH_DISABLE_ITEM_DROP);
    }

    public boolean isVanished() {
        return getPropertyState(UserPropertyType.VANISHED);
    }

    public boolean isFlying() {
        return getPropertyState(UserPropertyType.FLYING);
    }

    public boolean getPropertyState(UserPropertyType type) {
        var property = getProperty(type);
        return property != null ? property.getValue() : getDefaultValue(type);
    }

    public void confirmChatMessage(NamespacedKey key, Boolean value) {
        var base = getBase();
        if (base == null) return;
        base.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, value);
    }

    public boolean hasChatConfirmation(NamespacedKey key) {
        var base = getBase();
        if (base == null) return false;

        var value = base.getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN);
        if (value == null) return false;

        return value;
    }

    @Nullable
    public Player getBase() {
        return Bukkit.getPlayer(getUniqueId());
    }


    private boolean getDefaultValue(UserPropertyType type) {
        return type.getType() == 2 && ((Boolean) type.getDefaultValue());
    }

}
