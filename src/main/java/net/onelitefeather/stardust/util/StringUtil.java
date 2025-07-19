package net.onelitefeather.stardust.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException("Utility Class!");
    }

    public static String substringAfterLast(String delimiter, String missingDelimiterValue) {
        var index = missingDelimiterValue.lastIndexOf(delimiter);
        return index == -1 ? missingDelimiterValue : missingDelimiterValue.substring(index + 1);
    }

    public static Component colorText(String text) {
        return MiniMessage.miniMessage().deserialize(text);
    }

    public static Component secureComponent(Player player, Component origin) {
        return secureComponent(player, Constants.PERMISSION_SECURE_MESSAGE, origin);
    }

    public static Component secureComponent(Player player, String permission, Component origin) {
        var hasPerm = player.hasPermission(permission);
        return origin.clickEvent(hasPerm ? origin.clickEvent() : null)
            .hoverEvent(hasPerm ? origin.hoverEvent() : null);
    }

}
