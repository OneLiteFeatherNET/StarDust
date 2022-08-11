package net.onelitefeather.stardust.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import net.onelitefeather.stardust.FeatherEssentials;
import net.onelitefeather.stardust.api.user.IUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketListener {

    private final FeatherEssentials featherEssentials;
    private final ProtocolManager protocolManager;

    public PacketListener(FeatherEssentials featherEssentials) {
        this.featherEssentials = featherEssentials;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        handle();
    }

    public void handle() {

        List<UUID> loginQueue = new ArrayList<>();
        protocolManager.addPacketListener(new PacketAdapter(this.featherEssentials, ListenerPriority.HIGHEST, PacketType.Play.Server.LOGIN) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!loginQueue.contains(event.getPlayer().getUniqueId())) {
                    loginQueue.add(event.getPlayer().getUniqueId());
                }
            }
        });

        protocolManager.addPacketListener(new PacketAdapter(this.featherEssentials, ListenerPriority.HIGHEST, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
            @Override
            public void onPacketSending(PacketEvent event) {

                Player player = event.getPlayer();
                IUser user = featherEssentials.getUserManager().getUser(player.getUniqueId());
                if (user != null) {
                    if (user.isVanished()) {
                        if (player.hasPermission("featheressentials.command.vanish")) {
                            user.hidePlayer();
                            event.setCancelled(true);
                        } else {
                            user.toggleVanish();
                        }
                    }
                }

                featherEssentials.getServer().getOnlinePlayers().forEach(players -> {
                    IUser onlineUser = featherEssentials.getUserManager().getUser(players.getUniqueId());
                    if (onlineUser != null) {
                        if (onlineUser.isVanished()) {
                            onlineUser.hidePlayer();
                        }
                    }
                });
            }
        });

        protocolManager.addPacketListener(new PacketAdapter(this.featherEssentials, ListenerPriority.HIGHEST, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {

                PacketContainer packetContainer = event.getPacket();
                List<PlayerInfoData> playerInfoDataList = packetContainer.getPlayerInfoDataLists().read(0);
                EnumWrappers.PlayerInfoAction playerInfoAction = packetContainer.getPlayerInfoAction().read(0);

                if (playerInfoAction == EnumWrappers.PlayerInfoAction.UPDATE_LATENCY) {
                    hideVanishedPlayers(event, playerInfoDataList);
                }

                if (playerInfoAction == EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
                    if (loginQueue.contains(event.getPlayer().getUniqueId())) {
                        hideVanishedPlayers(event, playerInfoDataList);
                        loginQueue.remove(event.getPlayer().getUniqueId());
                    }
                }
            }
        });
    }

    public void unregister() {
        this.protocolManager.removePacketListeners(this.featherEssentials);
    }

    protected void hideVanishedPlayers(PacketEvent event, List<PlayerInfoData> playerInfoDataList) {
        for (PlayerInfoData playerInfoData : playerInfoDataList) {
            WrappedGameProfile wrappedGameProfile = playerInfoData.getProfile();
            Player player = Bukkit.getPlayer(wrappedGameProfile.getUUID());
            if (player != null) {
                if (player.hasMetadata("vanished")) {
                    for (MetadataValue metadataValue : player.getMetadata("vanished")) {
                        if (metadataValue.asBoolean()) {
                            event.setCancelled(true);
                            break;
                        }
                    }
                }
            }
        }
    }


}
