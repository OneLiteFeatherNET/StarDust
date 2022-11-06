package net.onelitefeather.stardust.extenstions

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player

fun Player.coloredDisplayName(): String = MiniMessage.miniMessage().serialize(
    LegacyComponentSerializer.legacyAmpersand()
        .deserialize(LegacyComponentSerializer.legacyAmpersand().serialize(this.displayName()))
)