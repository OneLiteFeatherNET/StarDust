package net.onelitefeather.stardust.user

import jakarta.persistence.*
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.*

@Entity
@Table
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    val uuid: String = UUID.randomUUID().toString(),

    @Column
    val name: String = "",

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    val properties: List<UserProperty> = arrayListOf()
) {

    constructor() : this(null)

    fun getProperty(propertyType: UserPropertyType): UserProperty? {
        return properties.firstOrNull { it.name == propertyType.name.lowercase() }
    }

    fun isItemDropDisabled(): Boolean {
        val propertyType = UserPropertyType.VANISH_DISABLE_ITEM_DROP
        val property = getProperty(propertyType) ?: return getDefaultValue(propertyType)
        return property.getValue<Boolean>() == false
    }

    fun isItemCollectDisabled(): Boolean {
        val propertyType = UserPropertyType.VANISH_DISABLE_ITEM_COLLECT
        val property = getProperty(propertyType) ?: return getDefaultValue(propertyType)
        return property.getValue<Boolean>() == false
    }

    fun isBuildingAllowed(): Boolean {
        if(!isVanished()) return true
        val propertyType = UserPropertyType.VANISH_ALLOW_BUILDING
        val property = getProperty(propertyType) ?: return getDefaultValue(propertyType)
        return property.getValue<Boolean>() == true
    }

    fun isPvPAllowed(): Boolean {
        if(!isVanished()) return true
        val propertyType = UserPropertyType.VANISH_ALLOW_PVP
        val property = getProperty(propertyType) ?: return getDefaultValue(propertyType)
        return property.getValue<Boolean>() == true
    }

    fun isVanished(): Boolean = getProperty(UserPropertyType.VANISHED)?.getValue() ?: false

    fun isFlying(): Boolean = getProperty(UserPropertyType.FLYING)?.getValue() ?: false

    fun getUniqueId(): UUID = UUID.fromString(uuid)

    fun getBase(): Player? = Bukkit.getPlayer(getUniqueId())

    fun confirmChatMessage(namespacedKey: NamespacedKey, value: Boolean) {
        val player = getBase() ?: return
        val container = player.persistentDataContainer
        container[namespacedKey, PersistentDataType.INTEGER] = if (value) 1 else 0
    }

    fun hasChatConfirmation(namespacedKey: NamespacedKey): Boolean {
        val player = getBase() ?: return false
        val container = player.persistentDataContainer
        if (!container.has(namespacedKey)) return false
        val value = container[namespacedKey, PersistentDataType.INTEGER] ?: return false
        return value == 1
    }



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (id != other.id) return false
        if (uuid != other.uuid) return false
        if (name != other.name) return false
        if (properties != other.properties) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + uuid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + properties.hashCode()
        return result
    }

    override fun toString(): String {
        return "User(id=$id, uuid='$uuid', name='$name', properties=$properties)"
    }

    private fun getDefaultValue(type: UserPropertyType): Boolean {
        return type.type.toInt() == 2 && type.defaultValue as Boolean
    }

}