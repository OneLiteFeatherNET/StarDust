package net.onelitefeather.stardust.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.onelitefeather.stardust.user.User;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;

public class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Utility Class!");
    }

    public static final double RADIUS_REMOVE_ENEMIES = 32.0;
    public static final String NOT_AVAILABLE_CONFIG_FALLBACK = "N/A (%s)";
    public static final String PERMISSION_SECURE_MESSAGE = "stardust.secure.message";

    public static final JoinConfiguration COMPONENT_JOIN_CONFIG =
            JoinConfiguration.separator(Component.translatable("commands.repair.repaired-items.separator"));
    public static final Vector DUMMY_VECTOR = new Vector(0.0, 0.0, 0.0);
    public static final User DUMMY_USER = new User(-1);
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
}
