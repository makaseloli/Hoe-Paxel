package io.github.makaseloli.hoepaxel;

import net.minecraft.network.chat.Component;

public enum PaxelWeaponMode {
    SWORD("sword"),
    SPEAR("spear");

    private final String serializedName;

    PaxelWeaponMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public Component displayName() {
        return Component.translatable("weapon_mode.hoepaxel." + serializedName);
    }

    public PaxelWeaponMode opposite() {
        return this == SWORD ? SPEAR : SWORD;
    }

    public static PaxelWeaponMode fromSerializedName(String serializedName) {
        for (PaxelWeaponMode value : values()) {
            if (value.serializedName.equals(serializedName)) {
                return value;
            }
        }
        return SWORD;
    }

    public static boolean isValidSerializedName(Object value) {
        return value instanceof String stringValue
                && (SWORD.serializedName.equals(stringValue) || SPEAR.serializedName.equals(stringValue));
    }
}
