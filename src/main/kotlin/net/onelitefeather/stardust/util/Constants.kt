package net.onelitefeather.stardust.util;

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat

val NOT_AVAILABLE_CONFIG_FALLBACK = "N/A (%s)"
val DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
val GSON: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create()
