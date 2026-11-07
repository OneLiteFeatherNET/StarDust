package net.onelitefeather.stardust.command.mapper;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.incendo.cloud.SenderMapper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;


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

    @SuppressWarnings({"NonExtendableApiUsage"})
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

        @Override
        public @NonNull Player getPlayerOrThrow() {
            return getLocation().getNearbyEntitiesByType(Player.class, 16)
                    .stream()
                    .filter(player -> player.getLocation().distance(getLocation()) <= 1.0D)
                    .findFirst()
                    .orElseThrow();
        }

        @Override
        public @NonNull Entity getEntityOrThrow() {
            return getLocation().getNearbyEntitiesByType(Entity.class, 16)
                    .stream()
                    .filter(entity -> entity.getLocation().distance(getLocation()) <= 1.0D)
                    .findFirst()
                    .orElseThrow();
        }


        @Override
        public @NotNull CommandSourceStack withLocation(@NotNull Location location) {
            return this;
        }

        @Override
        public @NotNull CommandSourceStack withExecutor(@NotNull Entity entity) {
            return this;
        }

        @Override
        public void sendReply(@NonNull ComponentLike message) {
            getPlayerOrThrow().sendMessage(message);
        }

        @Override
        public void sendSuccess(@NonNull ComponentLike message, boolean allowInformingAdmins) {
            getPlayerOrThrow().sendMessage(message);
        }

        @Override
        public void sendFailure(@NonNull ComponentLike message) {
            getPlayerOrThrow().sendMessage(message);
        }
    }

    @SuppressWarnings({"NonExtendableApiUsage"})
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

        @Override
        public @NonNull Player getPlayerOrThrow() {
            throw new IllegalCallerException("A Console cannot be an Player.");
        }

        @Override
        public @NonNull Entity getEntityOrThrow() {
            throw new IllegalCallerException("A Console cannot be an Entity.");
        }

        @Override
        public @NotNull CommandSourceStack withLocation(@NotNull Location location) {
            return this;
        }

        @Override
        public @NotNull CommandSourceStack withExecutor(@NotNull Entity entity) {
            return this;
        }

        @Override
        public void sendReply(@NonNull ComponentLike message) {
            Bukkit.getConsoleSender().sendMessage(message);
        }

        @Override
        public void sendSuccess(@NonNull ComponentLike message, boolean allowInformingAdmins) {
            Bukkit.getConsoleSender().sendMessage(message);
        }

        @Override
        public void sendFailure(@NonNull ComponentLike message) {
            Bukkit.getConsoleSender().sendMessage(message);
        }
    }

    @SuppressWarnings({"NonExtendableApiUsage"})
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

        @Override
        public @NonNull Player getPlayerOrThrow() {
            return this.player();
        }

        @Override
        public @NonNull Entity getEntityOrThrow() {
            return this.player();
        }

        @Override
        public @NotNull CommandSourceStack withLocation(@NotNull Location location) {
            return this;
        }

        @Override
        public @NotNull CommandSourceStack withExecutor(@NotNull Entity entity) {
            return this;
        }

        @Override
        public void sendReply(@NonNull ComponentLike message) {
            this.player.sendMessage(message);
        }

        @Override
        public void sendSuccess(@NonNull ComponentLike message, boolean allowInformingAdmins) {
            this.player.sendMessage(message);
        }

        @Override
        public void sendFailure(@NonNull ComponentLike message) {
            this.player.sendMessage(message);
        }
    }
}
