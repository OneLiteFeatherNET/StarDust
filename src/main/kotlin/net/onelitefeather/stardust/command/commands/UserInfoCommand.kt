package net.onelitefeather.stardust.command.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.parsers.Parser
import cloud.commandframework.annotations.suggestions.Suggestions
import cloud.commandframework.context.CommandContext
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.miniMessage
import net.onelitefeather.stardust.user.User
import net.onelitefeather.stardust.util.DUMMY_USER
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import java.util.Queue

@Suppress("unused")
class UserInfoCommand(private val stardustPlugin: StardustPlugin) {

    @CommandMethod("user info <player>")
    @CommandPermission("stardust.command.user.info")
    @CommandDescription("Get information about a User")
    fun handleCommand(
        commandSender: CommandSender,
        @Argument(value = "player", parserName = "user") user: User
    ) {

        val enabled = this.stardustPlugin.i18nService.getMessage("plugin.boolean-yes")
        val disabled = this.stardustPlugin.i18nService.getMessage("plugin.boolean-no")
        val prefix = this.stardustPlugin.i18nService.getPluginPrefix()

        val bukkitPlayer = user.getBase()
        val online = bukkitPlayer != null
        val invulnerable = bukkitPlayer != null && bukkitPlayer.isInvulnerable

        commandSender.sendMessage(miniMessage {
            stardustPlugin.i18nService.getMessage(
                "commands.user.info.uuid",
                *arrayOf(prefix, user.getUniqueId().toString())
            )
        })

        commandSender.sendMessage(miniMessage {
            stardustPlugin.i18nService.getMessage(
                "commands.user.info.name",
                *arrayOf(prefix, user.name)
            )
        })

        commandSender.sendMessage(miniMessage {
            stardustPlugin.i18nService.getMessage(
                "commands.user.info.flying",
                *arrayOf(prefix, if (user.properties.isFlying()) enabled else disabled)
            )
        })

        commandSender.sendMessage(miniMessage {
            stardustPlugin.i18nService.getMessage(
                "commands.user.info.vanished",
                *arrayOf(prefix, if (user.properties.isVanished()) enabled else disabled)
            )
        })

        commandSender.sendMessage(miniMessage {
            stardustPlugin.i18nService.getMessage(
                "commands.user.info.invulnerable",
                *arrayOf(prefix, if (invulnerable) enabled else disabled)
            )
        })

        commandSender.sendMessage(miniMessage {
            stardustPlugin.i18nService.getMessage(
                "commands.user.info.online",
                *arrayOf(prefix, if (online) enabled else disabled)
            )
        })

        commandSender.sendMessage(miniMessage {
            stardustPlugin.i18nService.getMessage(
                "commands.user.info.display-name",
                *arrayOf(prefix, user.getDisplayName())
            )
        })
    }

    @Suggestions("users")
    fun userSuggestions(commandContext: CommandContext<CommandSender>, input: String) =
        stardustPlugin.userService.getUsers()
            .filter { StringUtil.startsWithIgnoreCase(it.name, input.lowercase()) }.map { it.name }

    @Parser(name = "user", suggestions = "users")
    fun parseUsers(commandContext: CommandContext<CommandSender>, input: Queue<String>): User {
        val uuid = stardustPlugin.server.getPlayerUniqueId(input.remove()) ?: return DUMMY_USER
        return stardustPlugin.userService.getUser(uuid) ?: DUMMY_USER
    }
}