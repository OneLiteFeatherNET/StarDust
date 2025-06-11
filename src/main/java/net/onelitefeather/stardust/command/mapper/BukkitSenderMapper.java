package net.onelitefeather.stardust.command.mapper;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.incendo.cloud.SenderMapper;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings("UnstableApiUsage")
public class BukkitSenderMapper implements SenderMapper<CommandSourceStack, CommandSender> {
    private final ConsoleSenderMapper consoleSenderMapper = new ConsoleSenderMapper();

    @Override
    public @NotNull CommandSender map(CommandSourceStack base) {
        if (base.getSender() instanceof Player player) {
            return player;
        } else if (base.getSender() instanceof BlockCommandSender blockCommandSender) {
            return blockCommandSender;
        }
        return Bukkit.getConsoleSender();
    }

    @Override
    public @NotNull CommandSourceStack reverse(@NotNull CommandSender mapped) {
        if (mapped instanceof ConsoleCommandSender) return consoleSenderMapper;
        if (mapped instanceof Player player) return new PlayerSenderMapper(player);
        return new BlockSenderMapper((BlockCommandSender) mapped);
    }

    @SuppressWarnings({"UnstableApiUsage", "NonExtendableApiUsage"})
    private record BlockSenderMapper(BlockCommandSender blockCommandSender) implements CommandSourceStack {

        @Override
        public @NotNull Location getLocation() {
            return blockCommandSender.getBlock().getLocation();
        }

        @Override
        public @NotNull CommandSender getSender() {
            return this.blockCommandSender;
        }

        @Override
        public Entity getExecutor() {
            return null;
        }
    }

    @SuppressWarnings({"UnstableApiUsage", "NonExtendableApiUsage"})
    private static class ConsoleSenderMapper implements CommandSourceStack {
        @Override
        public @NotNull Location getLocation() {
            return Bukkit.getWorlds().getFirst().getSpawnLocation();
        }

        @Override
        public @NotNull CommandSender getSender() {
            return Bukkit.getConsoleSender();
        }

        @Override
        public Entity getExecutor() {
            return null;
        }
    }

    @SuppressWarnings({"UnstableApiUsage", "NonExtendableApiUsage"})
    private record PlayerSenderMapper(Player player) implements CommandSourceStack {

        @Override
        public @NotNull Location getLocation() {
            return player.getLocation();
        }

        @Override
        public @NotNull CommandSender getSender() {
            return this.player;
        }

        @Override
        public Entity getExecutor() {
            return this.player;
        }
    }
}
