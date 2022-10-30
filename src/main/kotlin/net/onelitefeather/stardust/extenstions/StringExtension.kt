package net.onelitefeather.stardust.extenstions;

import net.kyori.adventure.text.minimessage.MiniMessage


fun miniMessage(message: () -> String) = MiniMessage.miniMessage().deserialize(message())
