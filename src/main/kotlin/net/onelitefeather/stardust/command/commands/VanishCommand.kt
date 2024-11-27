package net.onelitefeather.stardust.command.commands

import net.kyori.adventure.text.Component
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.user.USER_PROPERTY_TYPE_VALUES
import net.onelitefeather.stardust.user.UserPropertyType
import net.onelitefeather.stardust.util.PlayerUtils
import net.onelitefeather.stardust.util.StringUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.context.CommandContext

@Suppress("unused")
class VanishCommand(private val stardustPlugin: StardustPlugin) : StringUtils, PlayerUtils {

    private val vanishProperties = USER_PROPERTY_TYPE_VALUES.filter(this::startsWith).map { it.toString() }
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

    @Command("vanish|v toggleProperty <property> [player]")
    @Permission("stardust.command.vanish.toggleproperty")
    @CommandDescription("Toggling vanish properties for yourself or someone else")
    fun commandVanishToggleProperty(commandSender: CommandSender,
                                    @Argument(value = "property", suggestions = "vanishProperties") property: UserPropertyType,
                                    @Greedy @Argument(value = "player") target: Player?) {
        if (target == null) {
            toggleProperty(commandSender, commandSender as Player, property)
        } else {

            val isSenderPlayer = commandSender is Player

            if(!isSenderPlayer) {
                //The commandSender is the console or a CommandBlock
                toggleProperty(commandSender, target, property)
                return
            }

            //The commandSender is another player.
            if(stardustPlugin.userService.playerVanishService.canSee(commandSender as Player, target)) {
                toggleProperty(commandSender, target, property)
            }
        }
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

    private fun toggleProperty(commandSender: CommandSender, target: Player, propertyType: UserPropertyType) {

        val user = stardustPlugin.userService.getUser(target.uniqueId) ?: return

        val property = user.getProperty(propertyType)

        //Get the default state of the boolean property
        val defaultState = propertyType.defaultValue as Boolean

        //Get the current state of the property
        val currentValue = if(property != null) property.getValue() else defaultState

        //The new value for the property
        val value = if(currentValue != null) !currentValue else defaultState

        stardustPlugin.userService.setUserProperty(user, propertyType, value)

        commandSender.sendMessage(Component.translatable("commands.vanish.property-set").arguments(
            stardustPlugin.getPluginPrefix(),
            Component.text(propertyType.friendlyName),
            Component.text(value),
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

    @Suggestions("vanishProperties")
    fun vanishProperties(context: CommandContext<CommandSender>, input: String): List<String> {
        return StringUtil.copyPartialMatches(input, vanishProperties, ArrayList(vanishProperties.size));
    }

    private fun startsWith(userPropertyType: UserPropertyType): Boolean {
        return userPropertyType.toString().startsWith("vanish_", true)
    }
}