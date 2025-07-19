package net.onelitefeather.stardust.util;

import java.text.SimpleDateFormat;

public class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Utility Class!");
    }

    public static final double RADIUS_REMOVE_ENEMIES = 32.0;
    public static final String PERMISSION_SECURE_MESSAGE = "stardust.secure.message";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public static final boolean INSIDE_TEST = Boolean.parseBoolean(System.getProperty("stardust.insideTest", "false"));
}
