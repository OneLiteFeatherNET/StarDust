package net.onelitefeather.stardust.api

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.UUID


interface IUser {

    fun getId(): Long

    fun getUniqueId(): UUID

    fun getName(): String

    fun setName(name: String): IUser

    fun setDisplayName(displayName: String)

    fun getDisplayName(): String

    fun setFlying(flying: Boolean): IUser

    fun isFlying(): Boolean

    fun setVanished(vanished: Boolean): IUser

    fun isVanished(): Boolean

    fun getBase(): Player?

    fun kick(message: Component): Boolean

    fun checkCanFly()


}