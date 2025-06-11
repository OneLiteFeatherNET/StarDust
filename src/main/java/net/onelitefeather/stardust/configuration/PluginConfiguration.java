package net.onelitefeather.stardust.configuration;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Objects;

public class PluginConfiguration {

    private final String itemSignMessage;
    private final List<Material> physicalBlocks;

    public PluginConfiguration(FileConfiguration configuration) {

        this.itemSignMessage = configuration.getString("item-signing.message", "");
        this.physicalBlocks = configuration.getStringList("physicalBlocks")
                .stream()
                .map(name -> Material.matchMaterial(name.toUpperCase()))
                .filter(Objects::nonNull).toList();
    }

    public String getItemSignMessage() {
        return itemSignMessage;
    }

    public List<Material> getPhysicalBlocks() {
        return physicalBlocks;
    }

    public boolean isPhysicalBlock(Material type) {
        return this.physicalBlocks.contains(type);
    }
}
