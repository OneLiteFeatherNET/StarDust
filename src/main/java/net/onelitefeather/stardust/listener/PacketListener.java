package net.onelitefeather.stardust.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import net.onelitefeather.stardust.StardustPlugin;

import java.util.List;

public class PacketListener {

    private static final List<EnumWrappers.PlayerInfoAction> WATCHED_ACTIONS = List.of(
            EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME,
            EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE,
            EnumWrappers.PlayerInfoAction.UPDATE_LATENCY
    );

    private final StardustPlugin stardustPlugin;
    private final ProtocolManager protocolManager;

    public PacketListener(StardustPlugin stardustPlugin) {
        this.stardustPlugin = stardustPlugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void unregister() {
        protocolManager.removePacketListeners(stardustPlugin);
    }

    public void register() {
        protocolManager.addPacketListener(new PacketAdapter(
                stardustPlugin,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.PLAYER_INFO
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                var packetContainer = event.getPacket();
                if (packetContainer.getPlayerInfoAction().size() == 0) return;

                EnumWrappers.PlayerInfoAction playerInfoAction = packetContainer.getPlayerInfoAction().read(0);
                if (!WATCHED_ACTIONS.contains(playerInfoAction)) return;

                List<PlayerInfoData> playerInfoDataList = packetContainer.getPlayerInfoDataLists().read(0);

                playerInfoDataList.removeIf(PacketListener.this::isVanished);
                packetContainer.getPlayerInfoDataLists().write(0, playerInfoDataList);
            }
        });
    }


    private boolean isVanished(PlayerInfoData data) {
        var user = this.stardustPlugin.getUserService().getUser(data.getProfile().getUUID());
        if (user == null) return false;
        return this.stardustPlugin.getUserService().getVanishService().isVanished(user.getBase());
    }
}
