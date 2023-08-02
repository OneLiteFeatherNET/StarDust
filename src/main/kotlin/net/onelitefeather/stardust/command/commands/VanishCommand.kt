package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import com.google.common.base.Preconditions
import net.kyori.adventure.text.minimessage.MiniMessage
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.user.UserPropertyType
import net.onelitefeather.stardust.util.PlayerUtils
import net.onelitefeather.stardust.util.StringUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("unused")
class VanishCommand(private val stardustPlugin: StardustPlugin) : StringUtils, PlayerUtils {

    @CommandMethod("vanish|v fakejoin [player]")
    @CommandPermission("stardust.command.vanish.fakejoin")
    @CommandDescription("Allows to perform a fake join")
    fun commandFakeJoin(
        commandSender: Player,
        @Greedy @Argument(value = "player") target: Player?
    ) {
        if (target == null) {
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<lang:listener.join-message:'${coloredDisplayName(commandSender)}'>"))
            return
        }
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<lang:listener.join-message:'${coloredDisplayName(target)}'>"))
    }

    @CommandMethod("vanish|v fakequit [player]")
    @CommandPermission("stardust.command.vanish.fakequit")
    @CommandDescription("Allows to perform a fake quit")
    fun commandFakeQuit(
        commandSender: Player,
        @Greedy @Argument(value = "player") target: Player?
    ) {
        if (target == null) {
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<lang:listener.quit-message:'${coloredDisplayName(commandSender)}'>"))
            return
        }
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<lang:listener.quit-message:'${coloredDisplayName(target)}'>"))
    }

    @CommandMethod("vanish|v nodrop [player]")
    @CommandPermission("stardust.command.vanish.nodrop")
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

    @CommandMethod("vanish|v noCollect [player]")
    @CommandPermission("stardust.command.vanish.nocollect")
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

    @CommandMethod("vanish|v [player]")
    @CommandPermission("stardust.command.vanish")
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
            propertyType == UserPropertyType.VANISH_DISABLE_ITEM_COLLECT || propertyType == UserPropertyType.VANISH_DISABLE_ITEM_DROP,
            "Invalid UserProperty type"
        )

        val user = stardustPlugin.userService.getUser(target.uniqueId) ?: return
        val property = stardustPlugin.userService.getUserProperty(user.properties, propertyType)
        val currentValue = property.getValue<Boolean>() ?: return

        stardustPlugin.userService.setUserProperty(user, propertyType, !currentValue)
        commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<lang:commands.vanish.property-set:'${stardustPlugin.getPluginPrefix()}':'${propertyType.friendlyName}':'${!currentValue}':'${target.name}'>"))
    }

    fun toggleVanish(commandSender: CommandSender, target: Player) {

        try {
            if (target != commandSender && !commandSender.hasPermission("stardust.command.vanish.others")) {
                commandSender.sendMessage(MiniMessage.miniMessage().deserialize("<lang:plugin.not-enough-permissions:'${stardustPlugin.getPluginPrefix()}'>"))
                return
            }

            val user = stardustPlugin.userService.getUser(target.uniqueId)
            if (user != null) {

                val state = stardustPlugin.userService.playerVanishService.toggle(target)
                val targetEnable = "<lang:commands.vanish.enable:'${stardustPlugin.getPluginPrefix()}':'${user.getDisplayName()}'>"
                val targetDisable = "<lang:commands.vanish.disable:'${stardustPlugin.getPluginPrefix()}':'${user.getDisplayName()}'>"
                if (commandSender != target) {
                    commandSender.sendMessage(
                        MiniMessage.miniMessage().deserialize(if (state) targetEnable else targetDisable)
                    )
                }

                target.sendMessage(MiniMessage.miniMessage().deserialize(if (state) targetEnable else targetDisable))
            }
        } catch (e: Exception) {
            this.stardustPlugin.getLogger().throwing(VanishCommand::class.java.simpleName, "toggleVanish", e)
        }
    }
}