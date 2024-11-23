package net.onelitefeather.stardust.command.commands

import com.google.common.base.Preconditions
import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.user.UserPropertyType
import net.onelitefeather.stardust.util.PlayerUtils
import net.onelitefeather.stardust.util.StringUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission

@Suppress("unused")
class VanishCommand(private val stardustPlugin: StardustPlugin) : StringUtils, PlayerUtils {

    @Command("vanish|v fakejoin [player]")
    @Permission("stardust.command.vanish.fakejoin")
    @CommandDescription("Allows to perform a fake join")
    fun commandFakeJoin(
        commandSender: Player,
        @Greedy @Argument(value = "player") target: Player?
    ) {
        if (target == null) {
            Bukkit.broadcast(Component.translatable("listener.join-message").arguments(commandSender.displayName()))
            return
        }

        Bukkit.broadcast(Component.translatable("listener.join-message").arguments(target.displayName()))
    }

    @Command("vanish|v fakequit [player]")
    @Permission("stardust.command.vanish.fakequit")
    @CommandDescription("Allows to perform a fake quit")
    fun commandFakeQuit(
        commandSender: Player,
        @Greedy @Argument(value = "player") target: Player?
    ) {
        if (target == null) {
            Bukkit.broadcast(Component.translatable("listener.quit-message").arguments(commandSender.displayName()))
            return
        }

        Bukkit.broadcast(Component.translatable("listener.quit-message").arguments(target.displayName()))
    }

    @Command("vanish|v nodrop [player]")
    @Permission("stardust.command.vanish.nodrop")
    @CommandDescription("Disable the ability to drop items")
    fun commandVanishNoDrop(
        commandSender: CommandSender,
        @Greedy @Argument(value = "player") target: Player?
    ) {

        if (target == null) {
            if (commandSender is Player) {
                toggleProperty(commandSender, commandSender, UserPropertyType.VANISH_DISABLE_ITEM_DROP)
            }
            return
        }

        toggleProperty(commandSender, target, UserPropertyType.VANISH_DISABLE_ITEM_DROP)
    }

    @Command("vanish|v noCollect [player]")
    @Permission("stardust.command.vanish.nocollect")
    @CommandDescription("Disable the ability to collect items")
    fun commandVanishNoCollect(
        commandSender: CommandSender,
        @Greedy @Argument(value = "player") target: Player?
    ) {

        if (target == null) {
            if (commandSender is Player) {
                toggleProperty(commandSender, commandSender, UserPropertyType.VANISH_DISABLE_ITEM_COLLECT)
            }
            return
        }

        toggleProperty(commandSender, target, UserPropertyType.VANISH_DISABLE_ITEM_COLLECT)
    }

    @Command("vanish|v [player]")
    @Permission("stardust.command.vanish")
    @CommandDescription("Make a player invisible for other players")
    fun handleCommand(commandSender: CommandSender, @Greedy @Argument(value = "player") target: Player?) {
        if (target == null) {
            toggleVanish(commandSender, commandSender as Player)
        } else {
            toggleVanish(commandSender, target)
        }
    }

    fun toggleProperty(commandSender: CommandSender, target: Player, propertyType: UserPropertyType) {

        Preconditions.checkArgument(
            propertyType == UserPropertyType.VANISH_DISABLE_ITEM_COLLECT
                    || propertyType == UserPropertyType.VANISH_DISABLE_ITEM_DROP,
            "Invalid UserProperty type"
        )

        val user = stardustPlugin.userService.getUser(target.uniqueId) ?: return
        val property = user.getProperty(propertyType) ?: return
        val currentValue = property.getValue<Boolean>() ?: return

        stardustPlugin.userService.setUserProperty(user, propertyType, !currentValue)

        commandSender.sendMessage(Component.translatable("commands.vanish.property-set").arguments(
            stardustPlugin.getPluginPrefix(),
            Component.text(propertyType.friendlyName),
            Component.text(!currentValue),
            Component.text(target.name)))
    }

    fun toggleVanish(commandSender: CommandSender, target: Player) {

        try {
            if (target != commandSender && !commandSender.hasPermission("stardust.vanish.others")) {
                commandSender.sendMessage(Component.translatable("plugin.not-enough-permissions").arguments(stardustPlugin.getPluginPrefix()))
                return
            }

            stardustPlugin.userService.playerVanishService.toggle(target)
        } catch (e: Exception) {
            this.stardustPlugin.logger.throwing(VanishCommand::class.java.simpleName, "toggleVanish", e)
        }
    }
}