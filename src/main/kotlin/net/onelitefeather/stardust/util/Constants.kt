package net.onelitefeather.stardust.util;

import net.onelitefeather.stardust.user.BukkitUser
import java.text.SimpleDateFormat
import java.util.UUID

const val NOT_AVAILABLE_CONFIG_FALLBACK = "N/A (%s)"
val DUMMY_USER = BukkitUser(-1, UUID.randomUUID().toString(), "Steve", vanished = false, flying = false)
val DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
