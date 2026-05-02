package io.github.makaseloli.hoepaxel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

public final class PaxelModeState {
    private static final Map<UUID, PaxelMode> SERVER_MAIN_HAND_MODES = new ConcurrentHashMap<>();
    private static final Map<UUID, PaxelWeaponMode> SERVER_MAIN_HAND_WEAPON_MODES = new ConcurrentHashMap<>();

    private PaxelModeState() {
    }

    public static PaxelMode getModeForHand(Player player, InteractionHand hand) {
        PaxelMode mainHandMode = getMainHandMode(player);
        return hand == InteractionHand.MAIN_HAND ? mainHandMode : mainHandMode.opposite();
    }

    public static PaxelMode getMainHandMode(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return SERVER_MAIN_HAND_MODES.getOrDefault(serverPlayer.getUUID(), PaxelMode.SHOVEL);
        }
        return ClientConfig.mainHandMode();
    }

    public static void setServerMainHandMode(ServerPlayer player, PaxelMode mode) {
        SERVER_MAIN_HAND_MODES.put(player.getUUID(), mode);
    }

    public static PaxelWeaponMode getMainHandWeaponMode(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return SERVER_MAIN_HAND_WEAPON_MODES.getOrDefault(serverPlayer.getUUID(), PaxelWeaponMode.SWORD);
        }
        return ClientConfig.mainHandWeaponMode();
    }

    public static void setServerMainHandWeaponMode(ServerPlayer player, PaxelWeaponMode mode) {
        SERVER_MAIN_HAND_WEAPON_MODES.put(player.getUUID(), mode);
    }
}
