package net.onelitefeather.stardust.util;

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.onelitefeather.stardust.user.User
import java.text.SimpleDateFormat
import java.util.UUID


const val NOT_AVAILABLE_CONFIG_FALLBACK = "N/A (%s)"
val DUMMY_USER = User(-1, UUID.randomUUID().toString(), "Steve", vanished = false, flying = false)
val DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
val GSON: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create()
