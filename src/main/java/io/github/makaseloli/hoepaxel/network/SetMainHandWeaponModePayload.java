package io.github.makaseloli.hoepaxel.network;

import io.github.makaseloli.hoepaxel.HoePaxelMod;
import io.github.makaseloli.hoepaxel.PaxelModeState;
import io.github.makaseloli.hoepaxel.PaxelWeaponMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetMainHandWeaponModePayload(boolean spearInMainHand) implements CustomPacketPayload {
    public static final Type<SetMainHandWeaponModePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HoePaxelMod.MOD_ID, "set_main_hand_weapon_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMainHandWeaponModePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    SetMainHandWeaponModePayload::spearInMainHand,
                    SetMainHandWeaponModePayload::new
            );

    public static SetMainHandWeaponModePayload fromMode(PaxelWeaponMode mode) {
        return new SetMainHandWeaponModePayload(mode == PaxelWeaponMode.SPEAR);
    }

    public PaxelWeaponMode mode() {
        return spearInMainHand ? PaxelWeaponMode.SPEAR : PaxelWeaponMode.SWORD;
    }

    @Override
    public Type<SetMainHandWeaponModePayload> type() {
        return TYPE;
    }

    public static void handle(SetMainHandWeaponModePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            PaxelModeState.setServerMainHandWeaponMode(player, payload.mode());
        }
    }
}
