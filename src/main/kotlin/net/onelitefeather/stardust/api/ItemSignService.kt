package net.onelitefeather.stardust.api

import net.kyori.adventure.text.Component

interface ItemSignService<I, P> {

    fun sign(lore: List<Component>, player: P): I

    fun isSigned(): Boolean

    fun setSigned(signed: Boolean)
}