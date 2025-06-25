package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.user.UserPropertyType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;

import java.util.ArrayList;
import java.util.List;

public class VanishCommand {

    private final StardustPlugin stardustPlugin;
    private final List<UserPropertyType> vanishProperties = List.of(
            UserPropertyType.VANISHED,
            UserPropertyType.VANISH_ALLOW_PVP,
            UserPropertyType.VANISH_ALLOW_PVP,
            UserPropertyType.VANISH_ALLOW_BUILDING,
            UserPropertyType.VANISH_DISABLE_ITEM_DROP,
            UserPropertyType.VANISH_DISABLE_ITEM_COLLECT);

    public VanishCommand(StardustPlugin stardustPlugin) {
        this.stardustPlugin = stardustPlugin;
    }

    @Command("vanish|v fakejoin [player]")
    @Permission("stardust.command.vanish.fakejoin")
    @CommandDescription("Allows to perform a fake join")
    public void commandFakeJoin(Player commandSender, @Greedy @Argument(value = "player") Player target) {
        if (target == null) {
            Bukkit.broadcast(Component.translatable("listener.join-message").arguments(commandSender.displayName()));
        } else {
            Bukkit.broadcast(Component.translatable("listener.join-message").arguments(target.displayName()));
        }
    }

    @Command("vanish|v fakequit [player]")
    @Permission("stardust.command.vanish.fakequit")
    @CommandDescription("Allows to perform a fake quit")
    public void commandFakeQuit(Player commandSender, @Greedy @Argument(value = "player") Player target) {
        if (target == null) {
            Bukkit.broadcast(Component.translatable("listener.quit-message").arguments(commandSender.displayName()));
        } else {
            Bukkit.broadcast(Component.translatable("listener.quit-message").arguments(target.displayName()));
        }
    }

    @Command("vanish|v toggleProperty <property> [player]")
    @Permission("stardust.command.vanish.toggleproperty")
    @CommandDescription("Toggling vanish properties for yourself or someone else")
    public void commandVanishToggleProperty(
            CommandSender commandSender,
            @Argument(value = "property", suggestions = "vanishProperties") UserPropertyType property,
            @Greedy @Argument(value = "player") Player target
    ) {
        if (target == null) {
            toggleProperty(commandSender, (Player) commandSender, property);
        } else {
            boolean isSenderPlayer = commandSender instanceof Player;
            if (!isSenderPlayer) {
                toggleProperty(commandSender, target, property);
                return;
            }
            if (stardustPlugin.getUserService().getVanishService().canSee((Player) commandSender, target)) {
                toggleProperty(commandSender, target, property);
            }
        }
    }

    @Command("vanish|v [player]")
    @Permission("stardust.command.vanish")
    @CommandDescription("Make a player invisible for other players")
    public void handleCommand(CommandSender commandSender, @Greedy @Argument(value = "player") Player target) {
        if (target == null) {
            toggleVanish(commandSender, (Player) commandSender);
        } else {
            toggleVanish(commandSender, target);
        }
    }

    private void toggleProperty(CommandSender commandSender, Player target, UserPropertyType propertyType) {
        var user = stardustPlugin.getUserService().getUser(target.getUniqueId());
        if (user == null) return;

        var property = user.getProperty(propertyType);
        boolean defaultState = (Boolean) propertyType.getDefaultValue();
        boolean currentValue = property != null ? (Boolean) property.getValue() : defaultState;
        boolean value = !currentValue;

        stardustPlugin.getUserService().setUserProperty(user, propertyType, value);

        commandSender.sendMessage(Component.translatable("commands.vanish.property-set").arguments(
                stardustPlugin.getPrefix(),
                Component.text(propertyType.getFriendlyName()),
                Component.text(value),
                Component.text(target.getName())
        ));
    }

    public void toggleVanish(CommandSender commandSender, Player target) {
        try {
            if (!target.equals(commandSender) && !commandSender.hasPermission("stardust.vanish.others")) {
                commandSender.sendMessage(Component.translatable("plugin.not-enough-permissions")
                        .arguments(stardustPlugin.getPrefix()));
                return;
            }
            stardustPlugin.getUserService().getVanishService().toggle(target);
        } catch (Exception e) {
            stardustPlugin.getLogger().throwing(VanishCommand.class.getSimpleName(), "toggleVanish", e);
        }
    }

    @Suggestions("vanishProperties")
    public List<String> vanishProperties(CommandContext<CommandSender> context, String input) {
        List<String> propertiesNames = this.vanishProperties.stream().map(Enum::toString).toList();
        return StringUtil.copyPartialMatches(input, propertiesNames, new ArrayList<>(propertiesNames.size()));
    }
}
