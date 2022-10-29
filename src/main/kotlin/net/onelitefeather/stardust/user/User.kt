package net.onelitefeather.stardust.user

import jakarta.persistence.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.onelitefeather.stardust.api.IUser
import net.onelitefeather.stardust.extenstions.miniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.hibernate.Hibernate
import java.util.UUID

@Entity
@Table
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    @Column val uuid: String?,
    @Column val lastKnownName: String?,
    @Column val vanished: Boolean = false,
    @Column val flying: Boolean = false
) : IUser {

    override fun getId(): Long {
        return id ?: -1
    }

    override fun getUniqueId(): UUID {
        return if (uuid != null) UUID.fromString(uuid) else UUID.randomUUID()
    }

    override fun getName(): String? {
        return lastKnownName
    }

    override fun setName(name: String): IUser {
        return this.copy(lastKnownName = name)
    }

    override fun setDisplayName(displayName: String) {
        val base = getBase() ?: return
        base.displayName(miniMessage {
            MiniMessage.miniMessage().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(displayName))
        })
    }

    override fun getDisplayName(): Component {
        val base = getBase() ?: return Component.empty()
        return base.displayName()
    }

    override fun setFlying(flying: Boolean): IUser {
        return this.copy(flying = flying)
    }

    override fun isFlying(): Boolean {
        return flying
    }

    override fun setVanished(vanished: Boolean): IUser {

        val base = getBase()
        if (base != null) {
            base.isSleepingIgnored = vanished
        }

        return this.copy(vanished = vanished)
    }

    override fun isVanished(): Boolean {
        return vanished
    }

    override fun getBase(): Player? {
        return Bukkit.getPlayer(getUniqueId())
    }

    override fun kick(message: Component): Boolean {
        val player = getBase() ?: return false
        player.kick(message)
        return true
    }

    override fun checkCanFly() {
        val player = getBase() ?: return
        if (player.hasPermission("featheressentials.join.flight") && isFlying()) {
            if (!player.allowFlight) player.allowFlight = true
        } else {
            if (player.allowFlight) {
                setFlying(false)
                player.allowFlight = false
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as User

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , uuid = $uuid , lastKnownName = $lastKnownName , vanished = $vanished , flying = $flying )"
    }
}