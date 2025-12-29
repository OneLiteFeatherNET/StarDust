package net.onelitefeather.stardust.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import net.onelitefeather.stardust.StardustPlugin;

import java.util.List;

public class VanishNoPacketListener implements PacketListener {

    private PacketListenerCommon packetListenerCommon;
    private final StardustPlugin stardustPlugin;

    public VanishNoPacketListener(StardustPlugin stardustPlugin) {
        this.stardustPlugin = stardustPlugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {

        // Identify what kind of packet it is.
        if (event.getPacketType() != PacketType.Play.Server.PLAYER_INFO) return;
        WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo(event);

        // Allow game mode updates to pass through.
        if (info.getAction() == WrapperPlayServerPlayerInfo.Action.UPDATE_GAME_MODE) return;

        List<WrapperPlayServerPlayerInfo.PlayerData> list = info.getPlayerDataList();
        list.removeIf(this::isVanished);
        info.setPlayerDataList(list);
    }

    public void unregister() {
        PacketEvents.getAPI().getEventManager().unregisterListener(this.packetListenerCommon);
    }

    public void register() {
        this.packetListenerCommon = PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
    }

    private boolean isVanished(WrapperPlayServerPlayerInfo.PlayerData data) {
        UserProfile userProfile = data.getUserProfile();
        if (userProfile == null) return false;
        var user = this.stardustPlugin.getUserService().getUser(userProfile.getUUID());
        if (user == null) return false;
        return this.stardustPlugin.getUserService().getVanishService().isVanished(user.getBase());
    }
}
