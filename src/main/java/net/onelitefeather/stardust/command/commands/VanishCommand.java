package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.util.Constants;
import net.onelitefeather.stardust.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

public class VanishCommand {

    private final StardustPlugin stardustPlugin;

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

    @Command("vanish|v toggle buildmode")
    @Permission("stardust.command.vanish.buildmode")
    @CommandDescription("Allows you to build while in vanish.")
    public void commandVanishBuildMode(Player player) {

        var stardustPlayer = this.stardustPlugin.getPlayerService().findById(player.getUniqueId());
        if (stardustPlayer == null) return;

        var newState = !stardustPlayer.canBuild();
        var translationKey = newState ? "commands.vanish.setting.buildmode.enabled" : "commands.vanish.setting.buildmode.disabled";

        this.stardustPlugin.getPlayerService().saveOrUpdate(stardustPlayer.withCanBuild(newState));
        player.sendMessage(Component.translatable(translationKey).arguments(Component.translatable("plugin.prefix")));
    }

    @Command("vanish|v toggle keepinv")
    @Permission("stardust.command.vanish.keepinv")
    @CommandDescription("Prevents your inventory from being changed while in vanish.")
    public void commandVanishKeepInventory(Player player) {
        var stardustPlayer = this.stardustPlugin.getPlayerService().findById(player.getUniqueId());
        if (stardustPlayer == null) return;

        var newState = !stardustPlayer.keepInv();
        var translationKey = newState ? "commands.vanish.setting.keepinv.enabled" : "commands.vanish.setting.keepinv.disabled";

        this.stardustPlugin.getPlayerService().saveOrUpdate(stardustPlayer.withKeepInventory(newState));
        player.sendMessage(Component.translatable(translationKey).arguments(Component.translatable("plugin.prefix")));
    }

    @Command("vanish|v toggle pve")
    @Permission("stardust.command.vanish.pve")
    @CommandDescription("Prevents you from attacking other players or entities while in vanish.")
    public void commandVanishPVE(Player player) {
        var stardustPlayer = this.stardustPlugin.getPlayerService().findById(player.getUniqueId());
        if (stardustPlayer == null) return;

        var newState = !stardustPlayer.canAttack();
        var translationKey = newState ? "commands.vanish.setting.pve.enabled" : "commands.vanish.setting.pve.disabled";

        this.stardustPlugin.getPlayerService().saveOrUpdate(stardustPlayer.withCanAttack(newState));
        player.sendMessage(Component.translatable(translationKey).arguments(Component.translatable("plugin.prefix")));
        if (!newState) PlayerUtil.removeEnemies(player, Constants.RADIUS_REMOVE_ENEMIES);
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

    public void toggleVanish(CommandSender commandSender, Player target) {
        try {
            if (!target.equals(commandSender) && !commandSender.hasPermission("stardust.vanish.others")) {
                commandSender.sendMessage(Component.translatable("plugin.not-enough-permissions")
                        .arguments(stardustPlugin.getPrefix()));
                return;
            }
            stardustPlugin.getVanishService().toggle(target.getUniqueId());
        } catch (Exception e) {
            stardustPlugin.getLogger().throwing(VanishCommand.class.getSimpleName(), "toggleVanish", e);
        }
    }

    private Component friendlyBooleanDisplay(Boolean bool) {
        return bool ? Component.translatable("boolean.enabled") : Component.translatable("boolean.disabled");
    }
}
