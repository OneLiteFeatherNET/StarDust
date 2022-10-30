package net.onelitefeather.stardust.listener

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.PlayerInfoData
import net.onelitefeather.stardust.StardustPlugin

class PacketListener(private val stardustPlugin: StardustPlugin) {

    var protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()

    fun unregister() {
        protocolManager.removePacketListeners(stardustPlugin)
    }

    fun register() {
        protocolManager.addPacketListener(object :
            PacketAdapter(stardustPlugin, ListenerPriority.HIGHEST, PacketType.Play.Server.PLAYER_INFO) {
            override fun onPacketSending(event: PacketEvent) {
                val packetContainer = event.packet
                val playerInfoDataList = packetContainer.playerInfoDataLists.read(0)
                packetContainer.playerInfoDataLists.write(0, hideVanishedPlayers(playerInfoDataList))
            }
        })
    }


    fun hideVanishedPlayers(playerInfoDataList: List<PlayerInfoData>): List<PlayerInfoData> =
        playerInfoDataList.filter { playerInfoData ->
            stardustPlugin.userService.getUser(playerInfoData.profile.uuid)?.isVanished() == true
        }
}