package io.github.makaseloli.hoepaxel;

import net.minecraft.network.chat.Component;

public enum PaxelMode {
    SHOVEL("shovel"),
    HOE("hoe");

    private final String serializedName;

    PaxelMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public Component displayName() {
        return Component.translatable("mode.hoepaxel." + serializedName);
    }

    public PaxelMode opposite() {
        return this == SHOVEL ? HOE : SHOVEL;
    }

    public static PaxelMode fromSerializedName(String serializedName) {
        for (PaxelMode value : values()) {
            if (value.serializedName.equals(serializedName)) {
                return value;
            }
        }
        return SHOVEL;
    }

    public static boolean isValidSerializedName(Object value) {
        return value instanceof String stringValue
                && (SHOVEL.serializedName.equals(stringValue) || HOE.serializedName.equals(stringValue));
    }
}

