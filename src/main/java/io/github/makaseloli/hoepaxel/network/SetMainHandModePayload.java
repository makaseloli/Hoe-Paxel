package io.github.makaseloli.hoepaxel.network;

import io.github.makaseloli.hoepaxel.HoePaxelMod;
import io.github.makaseloli.hoepaxel.PaxelMode;
import io.github.makaseloli.hoepaxel.PaxelModeState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetMainHandModePayload(boolean shovelInMainHand) implements CustomPacketPayload {
    public static final Type<SetMainHandModePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(HoePaxelMod.MOD_ID, "set_main_hand_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMainHandModePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    SetMainHandModePayload::shovelInMainHand,
                    SetMainHandModePayload::new
            );

    public static SetMainHandModePayload fromMode(PaxelMode mode) {
        return new SetMainHandModePayload(mode == PaxelMode.SHOVEL);
    }

    public PaxelMode mode() {
        return shovelInMainHand ? PaxelMode.SHOVEL : PaxelMode.HOE;
    }

    @Override
    public Type<SetMainHandModePayload> type() {
        return TYPE;
    }

    public static void handle(SetMainHandModePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            PaxelModeState.setServerMainHandMode(player, payload.mode());
        }
    }
}

