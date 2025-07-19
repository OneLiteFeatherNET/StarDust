package net.onelitefeather.stardust.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.PlayerVanishService;
import net.onelitefeather.stardust.user.UserPropertyType;
import net.onelitefeather.stardust.util.Constants;
import net.onelitefeather.stardust.util.PlayerUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

public class BukkitPlayerVanishService implements PlayerVanishService<Player> {

    private final StardustPlugin plugin;
    private final UserService userService;
    private final NamespacedKey vanishedKey;
    private static final String VANISH_SEE_OTHERS_PERMISSION = "stardust.vanish.others";

    public BukkitPlayerVanishService(UserService userService, StardustPlugin plugin) {
        this.plugin = plugin;
        this.userService = userService;
        this.vanishedKey = new NamespacedKey(plugin, "vanished");
    }

    @Override
    public void hidePlayer(Player player) {
        plugin.getServer().getScheduler().getMainThreadExecutor(plugin)
                .execute(() -> plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> {

                    if (!canSee(onlinePlayer, player)) {
                        onlinePlayer.hidePlayer(plugin, player);
                    }
                }));
    }

    @Override
    public void showPlayer(Player player) {
        plugin.getServer().getScheduler().getMainThreadExecutor(plugin)
                .execute(() -> plugin.getServer().getOnlinePlayers().forEach(onlinePlayer -> {

                    onlinePlayer.showPlayer(plugin, player);
                    onlinePlayer.sendMessage(Component.translatable("listener.join-message").arguments(player.displayName()));
                }));
    }

    @Override
    public boolean toggle(Player player) {

        var currentState = isVanished(player);

        if (currentState) {
            showPlayer(player);
            sendReappearedMessage(player);
        } else {
            hidePlayer(player);
            sendDisappearedMessage(player);
        }

        var newState = !currentState;
        setVanished(player, newState);
        togglePlayerProperties(player, newState);

        return newState;
    }

    @Override
    public boolean isVanished(Player player) {
        var vanished = player.getPersistentDataContainer().get(vanishedKey, PersistentDataType.BOOLEAN);
        if (vanished == null) return false;

        var user = userService.getUser(player.getUniqueId());
        if (user == null) return false;
        return user.isVanished() || vanished;
    }

    @Override
    public void setVanished(Player player, boolean vanished) {

        var user = userService.getUser(player.getUniqueId());
        if (user == null) return;

        this.userService.setUserProperty(user, UserPropertyType.VANISHED, vanished);
        player.getPersistentDataContainer().set(vanishedKey, PersistentDataType.BOOLEAN, vanished);
    }

    @Override
    public boolean handlePlayerJoin(Player player) {

        player.getServer().getOnlinePlayers().stream().filter(this::isVanished).forEach(this::hidePlayer);

        if (handleAutoVanish(player)) return true;

        if (isVanished(player)) {

            if (!player.hasPermission("stardust.command.vanish")) {
                setVanished(player, false);
                showPlayer(player);
                togglePlayerProperties(player, false);
                return true;
            }

            player.sendMessage(Component.translatable("vanish.join.self").arguments(plugin.getPrefix()));
            broadcastMessage(player,
                    Component.translatable("vanish.join.silently").arguments(vanishDisplayName(player)));
            hidePlayer(player);
            togglePlayerProperties(player, true);
        }

        return false;
    }

    @Override
    public void handlePlayerQuit(Player player) {
        if (!isVanished(player)) return;
        broadcastMessage(player,
                Component.translatable("vanish.quit.silently")
                        .arguments(plugin.getPrefix(), vanishDisplayName(player)));
    }

    @Override
    public boolean canSee(Player player, Player target) {
        if (!isGroupWeightBased()) return player.hasPermission(VANISH_SEE_OTHERS_PERMISSION);
        var playerGroupPriority = plugin.getLuckPermsService().getGroupPriority(player);
        var targetGroupPriority = plugin.getLuckPermsService().getGroupPriority(target);
        return playerGroupPriority >= targetGroupPriority;
    }

    private void sendReappearedMessage(Player player) {
        player.sendMessage(Component.translatable("vanish.self.reappeared")
                .arguments(plugin.getPrefix()));

        broadcastMessage(
                player,
                Component.translatable("vanish.reappeared")
                        .arguments(plugin.getPrefix(), vanishDisplayName(player))
        );
    }

    private void togglePlayerProperties(Player player, boolean vanished) {
        if (vanished) {
            PlayerUtil.removeEnemies(player, Constants.RADIUS_REMOVE_ENEMIES);
            player.setAllowFlight(true);
            player.setSleepingIgnored(true);
            player.setAffectsSpawning(false);
        } else {
            player.setAllowFlight(PlayerUtil.canEnterFlyMode(player));
            player.setSleepingIgnored(false);
            player.setAffectsSpawning(true);
        }
    }

    private void sendDisappearedMessage(Player player) {
        player.sendMessage(Component.translatable("vanish.self.disappeared")
                .arguments(plugin.getPrefix()));

        broadcastMessage(
                player,
                Component.translatable("vanish.disappeared")
                        .arguments(plugin.getPrefix(), vanishDisplayName(player))
        );

        plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> !p.hasPermission(VANISH_SEE_OTHERS_PERMISSION))
                .forEach(p -> p.sendMessage(Component.translatable("listener.quit-message").arguments(player.displayName())));
    }

    private boolean handleAutoVanish(Player player) {
        if (player.hasPermission("stardust.vanish.auto")) {
            var displayName = vanishDisplayName(player);

            setVanished(player, true);
            hidePlayer(player);
            togglePlayerProperties(player, true);
            player.sendMessage(Component.translatable("vanish.join.self").arguments(plugin.getPrefix()));

            broadcastMessage(
                    player,
                    Component.translatable("vanish.join.silently")
                            .arguments(plugin.getPrefix(), displayName)
            );
            return true;
        }
        return false;
    }

    private void broadcastMessage(Player player, Component message) {
        plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> !p.equals(player))
                .filter(p -> p.hasPermission(VANISH_SEE_OTHERS_PERMISSION))
                .forEach(p -> p.sendMessage(message));
    }

    private Component vanishDisplayName(Player player) {
        var team = player.getScoreboard().getPlayerTeam(player);
        if (team == null) return Component.text(player.getName());

        return MiniMessage.miniMessage().deserialize(player.getName()).color(team.color());
    }

    private boolean isGroupWeightBased() {
        if (!plugin.getLuckPermsService().isEnabled()) return false;
        return plugin.getConfig().getBoolean("vanish.group-weight-based-visibility", false);
    }
}
