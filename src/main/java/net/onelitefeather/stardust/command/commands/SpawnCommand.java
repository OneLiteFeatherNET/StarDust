package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.FeatherEssentials;
import net.onelitefeather.stardust.position.SpawnManager;
import net.onelitefeather.stardust.position.SpawnPoint;
import net.onelitefeather.stardust.util.TeleportCountdown;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public record SpawnCommand(FeatherEssentials featherEssentials, SpawnManager spawnManager) {

    @CommandMethod("spawn <name>")
    @CommandDescription("Teleports you to the Spawnpoint")
    @CommandPermission("featheressentials.commmand.spawn")
    public void handleSpawnCommand(Player player, @Argument(value = "name", suggestions = "spawnpoints") String name) {

        SpawnPoint spawnPoint = name == null ? this.spawnManager.getSpawnPoint("spawn") : this.spawnManager.getSpawnPoint(name);
        if (spawnPoint == null) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.spawn.not-found", this.featherEssentials.getPrefix(), name)));
            return;
        }

        Location location = spawnPoint.getWrappedLocation().toLocation();
        if (location != null) {

            if (!spawnPoint.hasPermission(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.spawn.not-enough-permission", this.featherEssentials.getPrefix(), name)));
                return;
            }

            Component message = MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("commands.spawn.teleported", this.featherEssentials.getPrefix(), name));
            if (player.hasPermission("featheressentials.teleport.countdown.bypass")) {
                player.teleport(location);
            } else {
                new TeleportCountdown(this.featherEssentials, 3, player, player.getLocation(), location, message);
            }

            player.sendMessage(MiniMessage.miniMessage().deserialize(String.format("You´re successfully teleported to spawn %s", spawnPoint.getName())));
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize(String.format("The World %s could not be found!", spawnPoint.getWrappedLocation().getWorldName())));
        }
    }

    @Suggestions("spawnpoints")
    public List<String> getSpawnPoints(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.spawnManager.getSpawnPoints().stream().map(SpawnPoint::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }
}
