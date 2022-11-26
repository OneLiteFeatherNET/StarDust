package net.onelitefeather.stardust.service;

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.onelitefeather.stardust.StardustPlugin
import net.onelitefeather.stardust.extenstions.colorText
import net.onelitefeather.stardust.extenstions.miniMessage
import net.onelitefeather.stardust.user.User
import net.onelitefeather.stardust.user.UserPropertyType
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.awt.Color

class PlayerNameTagService(private val stardustPlugin: StardustPlugin) {

    fun updateNameTag(player: Player) {
        stardustPlugin.server.scheduler.runTask(stardustPlugin, Runnable {

            val group = stardustPlugin.luckPermsService.getPrimaryGroup(player)
            val sortId = stardustPlugin.luckPermsService.getGroupSortId(group)
            val groupPrefix = stardustPlugin.luckPermsService.getPlayerGroupPrefix(player)
            val groupSuffix = stardustPlugin.luckPermsService.getPlayerGroupSuffix(player).colorText()
            val teamName = "$sortId${group.name}"

            val user = stardustPlugin.userService.getUser(player.uniqueId) ?: return@Runnable
            val vanished = user.properties.getProperty(UserPropertyType.VANISHED).getValue<Boolean>() == true
            val vanishTeam = "${sortId}vanished_${user.id}"
            val color = findScoreboardTeamColor(groupPrefix)

            player.displayName(miniMessage { groupPrefix.colorText().plus(player.name) })
            for (current in stardustPlugin.server.onlinePlayers) {
                initScoreboard(current)

                val scoreboard = current.scoreboard
                val team = if (vanished) {
                    createVanishTeam(
                        user,
                        scoreboard,
                        groupPrefix,
                        groupSuffix,
                        color,
                        vanishTeam
                    )
                } else if (scoreboard.getTeam(teamName) == null) {
                    scoreboard.registerNewTeam(teamName)
                } else {
                    scoreboard.getTeam(teamName)
                } ?: return@Runnable

                if (!vanished) {
                    team.prefix(miniMessage { groupPrefix.colorText() })
                    team.suffix(miniMessage { groupSuffix })
                }

                team.color(color)
                team.addEntry(player.name)
            }
        })
    }

    private fun initScoreboard(player: Player) {
        if (player.scoreboard == stardustPlugin.server.scoreboardManager.mainScoreboard) {
            player.scoreboard = stardustPlugin.server.scoreboardManager.newScoreboard
        }
    }

    fun findScoreboardTeamColor(text: String): NamedTextColor {

        var textColor = TextColor.color(Color.WHITE.rgb)
        if (text.contains(LegacyComponentSerializer.AMPERSAND_CHAR)) {
            val legacyFormat = LegacyComponentSerializer.parseChar(text.last())
            if (legacyFormat != null) {
                val fromLegacy = legacyFormat.color()
                if (fromLegacy != null) {
                    textColor = fromLegacy
                }
            }
        }

        return NamedTextColor.nearestTo(textColor)
    }

    private fun createVanishTeam(
        user: User,
        scoreboard: Scoreboard,
        prefix: String,
        suffix: String,
        namedTextColor: NamedTextColor,
        teamName: String
    ): Team {
        scoreboard.getTeam(teamName)?.unregister()
        val team = scoreboard.registerNewTeam(teamName)
        team.color(namedTextColor)
        team.prefix(miniMessage { " <white>[<${namedTextColor}>V</${namedTextColor}>]</white> ".plus(prefix).colorText() }.decorate(TextDecoration.ITALIC))
        team.suffix(miniMessage { suffix.colorText() })

        team.addEntry(user.name)
        return team
    }
}
