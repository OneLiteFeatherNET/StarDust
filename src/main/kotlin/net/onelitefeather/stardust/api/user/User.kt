package net.onelitefeather.stardust.api.user

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.UUID


interface User {

    fun getId(): Long

    fun getUniqueId(): UUID

    fun getName(): String

    fun setName(name: String): User

    fun setDisplayName(displayName: String)

    fun getDisplayName(): String

    fun setFlying(flying: Boolean): User

    fun isFlying(): Boolean

    fun setVanished(vanished: Boolean): User

    fun isVanished(): Boolean

    fun getBase(): Player?

    fun kick(message: Component): Boolean

    fun checkCanFly()


}