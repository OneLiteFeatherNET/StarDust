package net.onelitefeather.stardust.configuration

import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration

class PluginConfiguration(configuration: FileConfiguration) {

    var itemSignMessage: String = configuration.getString("item-signing.message", "")!!
    var physicalBlocks: List<Material> = configuration.getStringList("physicalBlocks").mapNotNull { Material.matchMaterial(it.uppercase()) }

    fun itemSignMessage() = itemSignMessage

    fun physicalBlocks() = physicalBlocks



}

