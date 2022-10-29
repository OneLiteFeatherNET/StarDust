package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.position.SpawnManager;
import net.onelitefeather.stardust.position.SpawnPoint;
import net.onelitefeather.stardust.position.WrappedLocation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public record SetSpawnCommand(StardustPlugin stardustPlugin, SpawnManager spawnManager) {

    @CommandMethod("setspawn <name> <defaultSpawn> [permission]")
    @CommandDescription("Set the World spawnpoint or create a custom spawnpoint")
    @CommandPermission("featheressentials.command.setspawn.set")
    public void handleSetSpawn(Player player, @Argument(value = "name") String name, @Argument(value = "permission") String permission, @Argument(value = "defaultSpawn") boolean defaultSpawn) {
        if (this.spawnManager.setSpawnPoint(name, WrappedLocation.fromLocation(player.getLocation()), permission, defaultSpawn)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.setspawn.spawn-set-success", this.stardustPlugin.getPrefix(), name)));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.setspawn.spawn-set-failure", this.stardustPlugin.getPrefix(), name)));
        }
    }

    @CommandMethod("removespawn <name>")
    @CommandDescription("Remove a Spawnpoint")
    @CommandPermission("featheressentials.command.setspawn.remove")
    public void handleRemoveSpawn(Player player, @Argument(value = "name", suggestions = "spawnpoints") String name) {

        if (this.stardustPlugin.getSpawnManager().removeSpawnPoint(name)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.setspawn.spawn-remove-success", this.stardustPlugin.getPrefix(), name)));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(this.stardustPlugin.getMessage("commands.setspawn.spawn-remove-failure", this.stardustPlugin.getPrefix(), name)));
        }
    }

    @Suggestions("spawnpoints")
    public List<String> getSpawnPoints(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.spawnManager.getSpawnPoints().stream().map(SpawnPoint::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }
}

