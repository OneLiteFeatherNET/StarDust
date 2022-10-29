package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Quoted;
import cloud.commandframework.annotations.specifier.Range;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpawnMobCommand {

    private final StardustPlugin stardustPlugin;
    private final List<EntityType> entityTypes;

    public SpawnMobCommand(StardustPlugin stardustPlugin) {
        this.stardustPlugin = stardustPlugin;
        this.entityTypes = Arrays.stream(EntityType.values()).filter(entityType -> entityType.isAlive() && entityType.isSpawnable()).toList();
    }

    @CommandMethod("spawnmob <type> <amount>")
    @CommandPermission("featheressentials.command.spawnmob")
    @CommandDescription("Spawn a Mob with the given Type.")
    public void onCommand(Player player, @Argument(value = "type", suggestions = "entity_types") @Quoted String entityType, @Range(min = "1", max = "25") @Argument(value = "amount") int amount) {

        Location location = player.getLocation();

        if (!entityType.contains(",")) {
            spawn(location, EntityType.valueOf(entityType), amount);
            player.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.spawnmob.success", this.stardustPlugin.getPrefix(), entityType, amount)));
        } else {

            String[] strings = entityType.split(",");
            Entity spawnedMob = null;
            Entity spawnedMount;

            for (int i = 0; i < strings.length; i++) {

                if (i == 0) {
                    spawnedMob = location.getWorld().spawnEntity(location, EntityType.valueOf(strings[0].toUpperCase()));
                }

                int next = i + 1;
                if (next < strings.length) {
                    EntityType nextType = EntityType.valueOf(strings[next].toUpperCase());
                    spawnedMount = location.getWorld().spawnEntity(location, nextType);
                    spawnedMob.addPassenger(spawnedMount);
                    spawnedMob = spawnedMount;
                }
            }

            player.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.spawnmob.success", this.stardustPlugin.getPrefix(), strings[0], 1)));
        }
    }

    @Suggestions("entity_types")
    public List<String> getEntityTypes(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.entityTypes.stream().map(EntityType::name).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(entityTypes.size()));
    }

    private void spawn(Location location, EntityType type, int amount) {
        while (amount > 0) {
            Entity spawned = location.getWorld().spawnEntity(location, type);
            if (spawned instanceof LivingEntity livingEntity) {
                EntityEquipment entityEquipment = livingEntity.getEquipment();
                if (entityEquipment != null) {
                    entityEquipment.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
                    entityEquipment.setBootsDropChance(0);
                }
            }

            amount--;
        }
    }

    private EntityType getEntityType(String type) {
        return Arrays.stream(EntityType.values()).filter(entityType -> entityType.name().equalsIgnoreCase(type) && entityType.isSpawnable() && entityType.isAlive()).findFirst().orElse(null);
    }


}
