package net.onelitefeather.stardust.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.player.StardustPlayer;
import net.onelitefeather.stardust.api.player.setting.SettingValueCodec;
import net.onelitefeather.stardust.api.registry.SettingRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.parser.Parser;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class PlayerInfoCommand {

    private final StardustPlugin plugin;
    private static final String DATE_PATTERN = "dd.MM.yyyy";
    private static final String DAY_TIME_PATTERN = "HH:mm:ss";

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN).withZone(ZONE_ID);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DAY_TIME_PATTERN).withZone(ZONE_ID);

    public PlayerInfoCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("stardust player info <player>")
    @Permission("stardust.command.player.info")
    @CommandDescription("Shows a player's StarDust settings")
    public void handleInfoCommand(CommandSender sender, @Argument(value = "player", parserName = "stardust_player_parser") StardustPlayer stardustPlayer) {

        if (!this.plugin.getPlayerService().playerExists(stardustPlayer.getUniqueId())) {
            sender.sendMessage(Component.translatable("no-player-found").arguments(
                    Component.translatable("plugin.prefix"),
                    Component.text(stardustPlayer.getName())));
            return;
        }

        var lastUpdatedMessage = Component.translatable("player.last-updated").arguments(Component.text(DATE_FORMATTER.format(stardustPlayer.getUpdatedAt())), Component.text(TIME_FORMATTER.format(stardustPlayer.getUpdatedAt())));
        var createdAtMessage = Component.translatable("player.created-at").arguments(Component.text(DATE_FORMATTER.format(stardustPlayer.getCreatedAt())), Component.text(TIME_FORMATTER.format(stardustPlayer.getCreatedAt())));

        var message = Component.translatable("command.playerinfo.entry.value.hover")
                .arguments(Component.text(stardustPlayer.getUniqueId().toString()), lastUpdatedMessage, createdAtMessage, Component.text(stardustPlayer.getName()));
        sender.sendMessage(Component.translatable("plugin.prefix").appendSpace().append(message));


        stardustPlayer.getSettings().forEach((key, value) -> {
            var settingKey = SettingRegistry.getSettingKey(key);
            sendSetting(sender, settingKey.translationKey(),
                    SettingValueCodec.decode(settingKey.type(),
                            value.value()));
        });
    }

    @Parser(name = "stardust_player_parser", suggestions = "stardust_player_suggestions")
    public StardustPlayer parsePlayer(CommandContext<CommandSender> context, CommandInput input) {
        var username = input.readString();
        var uuid = context.sender().getServer().getPlayerUniqueId(username);

        var player = this.plugin.getPlayerService().findById(uuid);
        return player != null ? player : StardustPlayer.DUMMY.withName(username);
    }

    @Suggestions("stardust_player_suggestions")
    public List<String> playerNames(CommandContext<CommandSender> context, String input) {
        List<String> playerNames = this.plugin.getPlayerService().findOnlinePlayers()
                .stream()
                .map(StardustPlayer::getName)
                .toList();

        return StringUtil.copyPartialMatches(input, playerNames, new ArrayList<>(playerNames.size()));
    }


    private void sendSetting(CommandSender sender, String key, Object value) {

        TranslatableComponent valueArgument = Component.translatable("command.playerinfo.entry.value").arguments(Component.text(value.toString()));
        if (value instanceof Boolean bool) {
            valueArgument = bool ? Component.translatable("boolean.enabled") : Component.translatable("boolean.disabled");
        }

        var entryMessage = Component.translatable("command.playerinfo.entry.key").arguments(Component.translatable(key));

        sender.sendMessage(Component.translatable("plugin.prefix").appendSpace().append(entryMessage).appendSpace().append(valueArgument));

    }
}
