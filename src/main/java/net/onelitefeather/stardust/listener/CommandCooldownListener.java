package net.onelitefeather.stardust.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.onelitefeather.stardust.StardustPlugin;
import net.onelitefeather.stardust.api.CommandCooldownService;
import net.onelitefeather.stardust.util.StringUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandCooldownListener implements Listener {

    private final StardustPlugin plugin;
    private final CommandCooldownService cooldownService;

    public CommandCooldownListener(StardustPlugin plugin) {
        this.plugin = plugin;
        this.cooldownService = plugin.getCooldownService();
    }

    @EventHandler
    public void handle(PlayerCommandPreprocessEvent event) {

        var player = event.getPlayer();

        var commandRaw = event.getMessage().replaceFirst("^/", "");
        var args = commandRaw.split(" ");

        if (args.length == 0) return;

        var labelOrCommand = args[0].toLowerCase();
        var command = labelOrCommand.contains(":") ? StringUtil.substringAfterLast(labelOrCommand, ":") : labelOrCommand;

        if(!cooldownService.hasCommandCooldown(command)) return;


        if(player.hasPermission("stardust.commandcooldown.bypass") && plugin.getConfig().getBoolean("settings.use-cooldown-bypass")) return;
        var commandCooldown = cooldownService.getCommandCooldown(player.getUniqueId(), command);

        if (commandCooldown != null && !commandCooldown.isOver()) {

            player.sendMessage(Component.translatable("plugin.command-cooldowned").arguments(
                    plugin.getPrefix(),
                    getRemainingTime(commandCooldown.getExecutedAt())));
            event.setCancelled(true);
            return;
        }

        var cooldownData = cooldownService.getCooldownData(command);
        if (cooldownData != null) {
            this.cooldownService.addCommandCooldown(
                    player.getUniqueId(),
                    cooldownData.getCommandName(),
                    cooldownData.getTime(),
                    cooldownData.getTimeUnit()
            );
        }
    }

    private Component getRemainingTime(Long time) {
        var diff =  Math.abs(time - System.currentTimeMillis());
        var seconds = diff / 1000 % 60;
        var minutes = diff / (1000 * 60) % 60;
        var hours = diff / (1000 * 60 * 60) % 24;
        var days = diff / (1000 * 60 * 60 * 24);

        if(days > 0) {
            return Component.translatable("remaining-time.days").arguments(
                    TranslationArgument.numeric(days),
                    TranslationArgument.numeric(hours),
                    TranslationArgument.numeric(minutes),
                    TranslationArgument.numeric(seconds));
        }

        if(hours > 0) {
            return Component.translatable("remaining-time.hours").arguments(
                    TranslationArgument.numeric(hours),
                    TranslationArgument.numeric(minutes),
                    TranslationArgument.numeric(seconds));
        }

        if(minutes > 0) {
            return Component.translatable("remaining-time.minutes").arguments(
                    TranslationArgument.numeric(minutes),
                    TranslationArgument.numeric(seconds));
        }


        return Component.translatable("remaining-time.seconds").arguments(TranslationArgument.numeric(seconds));
    }
}
