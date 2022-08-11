package net.onelitefeather.stardust.tasks;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.onelitefeather.stardust.FeatherEssentials;
import net.onelitefeather.stardust.api.user.IUser;

public record UserTask(FeatherEssentials featherEssentials) implements Runnable {
    @Override
    public void run() {
        this.featherEssentials.getServer().getOnlinePlayers().forEach(player -> {
            IUser user = this.featherEssentials.getUserManager().getUser(player.getUniqueId());
            if(user != null) {
                if(user.isVanished()) {
                    player.sendActionBar(MiniMessage.miniMessage().deserialize(this.featherEssentials.getMessage("plugin.vanish-actionbar")));
                }
            }
        });
    }
}
