package net.onelitefeather.stardust.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum UserPropertyType {

    FLYING("Flying", false, (byte) 2),
    VANISHED("Vanished", false, (byte) 2),
    VANISH_DISABLE_ITEM_DROP("No-Drop", true, (byte) 2),
    VANISH_DISABLE_ITEM_COLLECT("No-Collect", true, (byte) 2),
    VANISH_ALLOW_BUILDING("Allow Building", false, (byte) 2),
    VANISH_ALLOW_PVP("Allow PvP", false, (byte) 2);

    private final String friendlyName;
    private final Object defaultValue;
    private final byte type;

    UserPropertyType(String friendlyName, Object defaultValue, byte type) {
        this.friendlyName = friendlyName;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public String getName() {
        return toString().toLowerCase();
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public byte getType() {
        return type;
    }

    private static final UserPropertyType[] VALUES = values();

    public static UserPropertyType getByName(String upperCase) {
        return Arrays.stream(VALUES)
                .filter(type -> type.toString().equalsIgnoreCase(upperCase))
                .findFirst().orElse(null);
    }

    public static List<UserProperty> getDefaultUserProperties() {
        List<UserProperty> list = new ArrayList<>();
        for (UserPropertyType type : VALUES) {
            list.add(new UserProperty(
                    null,
                    type.toString().toLowerCase(),
                    type.defaultValue.toString(),
                    type.type,
                    null
            ));
        }
        return list;
    }
}
