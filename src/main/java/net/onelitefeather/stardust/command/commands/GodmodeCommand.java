package net.onelitefeather.stardust.command.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record GodmodeCommand(FeatherEssentials featherEssentials) {

    @CommandMethod("godmode [player]")
    @CommandPermission("featheressentials.command.godmode")
    @CommandDescription("Makes a player invulnerable to everything :D")
    public void onCommand(@NotNull CommandSender commandSender, @Argument(value = "player", suggestions = "players") String targetName) {

        Player target = null;
        if (null != targetName) {
            target = commandSender.getServer().getPlayer(targetName);
        } else {
            if (commandSender instanceof Player player) {
                target = player;
            }
        }

        if (target == null) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.player-not-found", this.featherEssentials.getPrefix(), targetName)));
            return;
        }

        if (!target.equals(commandSender)) {
            if (!commandSender.hasPermission("featheressentials.command.godmode.others")) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.not-enough-permissions", this.featherEssentials.getPrefix())));
                return;
            }
        }

        String displayName = this.featherEssentials.getVaultHook().getPlayerDisplayName(target);
        target.setInvulnerable(!target.isInvulnerable());

        if (target.isInvulnerable()) {
            for (LivingEntity livingEntity : target.getWorld().getLivingEntities()) {
                if (livingEntity instanceof Mob mob) {
                    if (mob.getTarget() != null && mob.getTarget().equals(target)) {
                        mob.setTarget(null);
                    }
                }
            }
        }

        String enabled = this.featherEssentials.getMessage("commands.god-mode.enable", this.featherEssentials.getPrefix(), displayName);
        String disabled = this.featherEssentials.getMessage("commands.god-mode.disable", this.featherEssentials.getPrefix(), displayName);

        String targetEnabled = this.featherEssentials.getMessage("commands.god-mode.target.enable", this.featherEssentials.getPrefix());
        String targetDisabled = this.featherEssentials.getMessage("commands.god-mode.target.disable", this.featherEssentials.getPrefix());

        target.sendMessage(MiniMessage.miniMessage().deserialize( target.isInvulnerable() ? targetEnabled : targetDisabled) );
        if (!commandSender.equals(target)) {
            commandSender.sendMessage(MiniMessage.miniMessage().deserialize( target.isInvulnerable() ? enabled : disabled));
        }
    }

    @Suggestions(value = "players")
    public List<String> getPlayers(CommandContext<CommandSender> context, String input) {
        List<String> strings = this.featherEssentials.getVisiblePlayers(context.getSender()).stream().map(HumanEntity::getName).toList();
        return StringUtil.copyPartialMatches(input, strings, new ArrayList<>(strings.size()));
    }
}
