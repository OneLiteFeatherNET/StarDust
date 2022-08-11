package net.onelitefeather.stardust.hook;

import de.dytanic.cloudnet.driver.permission.IPermissionGroup;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;
import de.dytanic.cloudnet.wrapper.Wrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import net.onelitefeather.stardust.FeatherEssentials;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class VaultHook {

    private final FeatherEssentials plugin;
    private Permission vaultPermission;
    private Chat vaultChat;
    private boolean cloudNetSupport;

    public VaultHook(FeatherEssentials plugin) {
        this.plugin = plugin;
        //this.initVaultSupport(); why duplicated call?
    }

    public void initVaultSupport() {
        Server server = this.plugin.getServer();
        if(server.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Chat> chatProvider = server.getServicesManager().getRegistration(Chat.class);
            if(chatProvider != null) {
                this.vaultChat = chatProvider.getProvider();
                this.plugin.getLogger().info("Using " + this.vaultChat.getName() + " as Chat provider.");
            }

            RegisteredServiceProvider<Permission> permissionProvider = server.getServicesManager().getRegistration(Permission.class);
            if(permissionProvider != null) {
                this.vaultPermission = permissionProvider.getProvider();
                this.plugin.getLogger().info("Using " + this.vaultPermission.getName() + " as Permission provider");
            }
        }
        this.cloudNetSupport = server.getPluginManager().isPluginEnabled("CloudNet-CloudPerms");
        if(!this.cloudNetSupport) {
            if(this.vaultPermission == null) {
                plugin.getLogger().severe("Could not find an Permission Plugin.");
                return;
            }
            FileConfiguration configuration = plugin.getConfig();
            for(String group : this.vaultPermission.getGroups()) {
                String path = String.format("groups.order.%s.priority", group);
                if(!configuration.isSet(path))
                    configuration.set(path, 100);
            }
        }

        plugin.saveConfig();
    }

    public FeatherEssentials getPlugin() {
        return plugin;
    }

    public Permission getVaultPermission() {
        return vaultPermission;
    }

    public boolean isEnabled() {
        return this.vaultPermission != null;
    }

    public boolean hasCloudNetSupport() {
        return this.cloudNetSupport;
    }

    public Chat getVaultChat() {
        return vaultChat;
    }

    public Component getDisplayName(Player player) {
        return MiniMessage.miniMessage().deserialize(getPlayerDisplayName(player));
    }

    public String getPlayerDisplayName(Player player) {
        return MiniMessage.miniMessage().serialize(isEnabled() ? LegacyComponentSerializer.legacyAmpersand().deserialize(this.vaultChat.getPlayerPrefix(player) + player.getName()) : player.displayName());
    }

    public int getGroupPriority(String group) {
        if(!this.cloudNetSupport)
            return this.plugin.getConfig().getInt("groups.order." + group + ".priority", 0);

        IPermissionGroup permissionGroup = Wrapper.getInstance().getPermissionManagement().getGroup(group);

        if(permissionGroup == null)
            permissionGroup = Wrapper.getInstance().getPermissionManagement().getDefaultPermissionGroup();

        return permissionGroup.getPotency();
    }

    public int getGroupPriority(UUID uuid) {
        if(!this.cloudNetSupport) {
            if(!isEnabled())
                return 0;

            Player player = this.plugin.getServer().getPlayer(uuid);
            if(player == null)
                return 0;
            return this.plugin.getConfig().getInt("group.order." + this.vaultPermission.getPrimaryGroup(player) + ".priority", 0);
        }

        IPermissionUser permissionUser = Wrapper.getInstance().getPermissionManagement().getUser(uuid);
        if(permissionUser == null)
            return 0;

        return Wrapper.getInstance().getPermissionManagement().getHighestPermissionGroup(permissionUser).getPotency();
    }

    public String getGroupPrefix(String group) {
        return this.vaultChat.getGroupPrefix("", group);
    }

}
