package net.onelitefeather.stardust.user;

import jakarta.persistence.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.jetbrains.annotations.Nullable;
import org.hibernate.annotations.Cache;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "stardust-user")
@NaturalIdCache(region = "stardust-user-naturalid")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @Column(unique = true, nullable = false, length = 36)
    private String uuid;

    @Column
    private String name;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "stardust-user-properties")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private List<UserProperty> properties;

    @Transient
    private transient UUID cachedUuid;

    @Transient
    private transient Map<String, UserProperty> propertyIndex;

    public User() {
        //Empty constructor for hibernate
    }

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
        if (cachedUuid == null) {
            cachedUuid = UUID.fromString(uuid);
        }
        return cachedUuid;
    }


    public String getName() {
        return name;
    }

    public User withName(String name) {
        this.name = name;
        return this;
    }

    public UserProperty getProperty(UserPropertyType type) {
        if (propertyIndex == null) {
            // built once per cached instance; tiny map, rebuilt after each DB (re)load
            propertyIndex = properties == null ? Map.of()
                    : properties.stream().collect(Collectors.toUnmodifiableMap(
                    UserProperty::getName, p -> p, (a, b) -> b));
        }
        return propertyIndex.get(type.getName());
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

        //Remove old data from the persistent container.
        var container = base.getPersistentDataContainer();
        if (container.has(key, PersistentDataType.INTEGER)) {
            container.remove(key);
            return false;
        }

        var value = container.get(key, PersistentDataType.BOOLEAN);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return uuid != null && uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

}
