package net.onelitefeather.stardust.command.commands;


import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

public class HealCommand {

    private static final int DEFAULT_PLAYER_FIRE_TICKS = 0;
    private static final boolean DEFAULT_ENTITY_HAS_VISUAL_FIRE = false;
    private static final int DEFAULT_PLAYER_FOOD_LEVEL = 20;
    private static final float DEFAULT_PLAYER_SATURATION_LEVEL = 5.0f;

    private final StardustPlugin plugin;

    public HealCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("heal [player]")
    @Permission("stardust.command.heal")
    @CommandDescription("Heal a player.")
    public void onCommand(CommandSender commandSender, @Greedy @Argument(value = "player") Player target) {
        if (commandSender instanceof Player player) {
            healPlayer(player, target != null ? target : player);
        } else {
            if (target == null) return;
            healPlayer(commandSender, target);
        }
    }

    private void healPlayer(CommandSender commandSender, Player target) {
        if (!commandSender.equals(target) && !commandSender.hasPermission("stardust.command.heal.others")) {
            commandSender.sendMessage(
                    Component.translatable("plugin.not-enough-permissions")
                            .arguments(plugin.getPrefix())
            );
            return;
        }

        AttributeInstance healthAttribute = target.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            target.setHealth(healthAttribute.getValue());
        }

        target.setFireTicks(DEFAULT_PLAYER_FIRE_TICKS);
        target.setVisualFire(DEFAULT_ENTITY_HAS_VISUAL_FIRE);
        target.setFoodLevel(DEFAULT_PLAYER_FOOD_LEVEL);
        target.setSaturation(DEFAULT_PLAYER_SATURATION_LEVEL);

        if (commandSender.equals(target)) {
            target.sendMessage(Component.translatable("commands.heal.target").arguments(plugin.getPrefix()));
        } else {
            target.sendMessage(Component.translatable("commands.heal.target").arguments(plugin.getPrefix()));
            commandSender.sendMessage(Component.translatable("commands.heal.success").arguments(plugin.getPrefix(), target.displayName()));
        }
    }
}
