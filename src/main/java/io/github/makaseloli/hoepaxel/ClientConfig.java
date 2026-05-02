package io.github.makaseloli.hoepaxel;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<String> MAIN_HAND_MODE;
    public static final ModConfigSpec.ConfigValue<String> MAIN_HAND_WEAPON_MODE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("controls");
        MAIN_HAND_MODE = builder.comment("Default right-click tool mode for paxels held in the main hand: shovel or hoe.")
                .define("mainHandMode", PaxelMode.SHOVEL.serializedName(), PaxelMode::isValidSerializedName);
        MAIN_HAND_WEAPON_MODE = builder.comment("Default combat mode for paxels held in either hand: sword or spear.")
                .define("mainHandWeaponMode", PaxelWeaponMode.SWORD.serializedName(), PaxelWeaponMode::isValidSerializedName);
        builder.pop();

        SPEC = builder.build();
    }

    private ClientConfig() {
    }

    public static PaxelMode mainHandMode() {
        return PaxelMode.fromSerializedName(MAIN_HAND_MODE.get());
    }

    public static PaxelWeaponMode mainHandWeaponMode() {
        return PaxelWeaponMode.fromSerializedName(MAIN_HAND_WEAPON_MODE.get());
    }
}
