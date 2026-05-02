package io.github.makaseloli.hoepaxel.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.makaseloli.hoepaxel.ClientConfig;
import io.github.makaseloli.hoepaxel.HoePaxelMod;
import io.github.makaseloli.hoepaxel.PaxelItem;
import io.github.makaseloli.hoepaxel.PaxelMode;
import io.github.makaseloli.hoepaxel.PaxelWeaponMode;
import io.github.makaseloli.hoepaxel.network.SetMainHandModePayload;
import io.github.makaseloli.hoepaxel.network.SetMainHandWeaponModePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = HoePaxelMod.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientEvents {
    private static final KeyMapping.Category KEY_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(HoePaxelMod.MOD_ID, "general"));
    private static final KeyMapping TOGGLE_MAIN_HAND_MODE = new KeyMapping(
            "key.hoepaxel.toggle_main_hand_mode",
            InputConstants.KEY_H,
            KEY_CATEGORY
    );
    private static final KeyMapping TOGGLE_MAIN_HAND_WEAPON_MODE = new KeyMapping(
            "key.hoepaxel.toggle_main_hand_weapon_mode",
            InputConstants.KEY_J,
            KEY_CATEGORY
    );

    private static PaxelMode lastSyncedMode;
    private static PaxelWeaponMode lastSyncedWeaponMode;
    private static PaxelMode lastShownMainHandMode;
    private static PaxelMode lastShownOffHandMode;
    private static boolean mainHandHadPaxel;
    private static boolean offHandHadPaxel;
    private static int suppressOverlayTicks;

    private NeoForgeClientEvents() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MAIN_HAND_MODE);
        event.register(TOGGLE_MAIN_HAND_WEAPON_MODE);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            resetClientState();
            return;
        }

        while (TOGGLE_MAIN_HAND_MODE.consumeClick()) {
            toggleMainHandMode(minecraft);
        }
        while (TOGGLE_MAIN_HAND_WEAPON_MODE.consumeClick()) {
            toggleMainHandWeaponMode(minecraft);
        }

        syncModeToServer(minecraft);
        syncWeaponModeToServer(minecraft);
        syncHeldCombatMode(minecraft);

        if (suppressOverlayTicks > 0) {
            suppressOverlayTicks--;
        }

        updateHandNotification(minecraft, InteractionHand.MAIN_HAND, ClientConfig.mainHandMode());
        updateHandNotification(minecraft, InteractionHand.OFF_HAND, ClientConfig.mainHandMode().opposite());
    }

    private static void toggleMainHandMode(Minecraft minecraft) {
        PaxelMode nextMode = ClientConfig.mainHandMode().opposite();
        ClientConfig.MAIN_HAND_MODE.set(nextMode.serializedName());
        ClientConfig.MAIN_HAND_MODE.save();

        suppressOverlayTicks = 8;
        sendModeToServer(nextMode);

        SystemToast.addOrUpdate(
                minecraft.getToastManager(),
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.translatable("toast.hoepaxel.mode_title"),
                Component.translatable("toast.hoepaxel.mode_body", nextMode.displayName())
        );
    }

    private static void toggleMainHandWeaponMode(Minecraft minecraft) {
        PaxelWeaponMode nextMode = ClientConfig.mainHandWeaponMode().opposite();
        ClientConfig.MAIN_HAND_WEAPON_MODE.set(nextMode.serializedName());
        ClientConfig.MAIN_HAND_WEAPON_MODE.save();

        sendWeaponModeToServer(nextMode);

        SystemToast.addOrUpdate(
                minecraft.getToastManager(),
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.translatable("toast.hoepaxel.weapon_mode_title"),
                Component.translatable("toast.hoepaxel.weapon_mode_body", nextMode.displayName())
        );
    }

    private static void syncModeToServer(Minecraft minecraft) {
        if (minecraft.getConnection() == null) {
            lastSyncedMode = null;
            return;
        }

        PaxelMode configuredMode = ClientConfig.mainHandMode();
        if (configuredMode != lastSyncedMode) {
            sendModeToServer(configuredMode);
        }
    }

    private static void syncWeaponModeToServer(Minecraft minecraft) {
        if (minecraft.getConnection() == null) {
            lastSyncedWeaponMode = null;
            return;
        }

        PaxelWeaponMode configuredMode = ClientConfig.mainHandWeaponMode();
        if (configuredMode != lastSyncedWeaponMode) {
            sendWeaponModeToServer(configuredMode);
        }
    }

    private static void sendModeToServer(PaxelMode mode) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            lastSyncedMode = null;
            return;
        }

        ClientPacketDistributor.sendToServer(SetMainHandModePayload.fromMode(mode));
        lastSyncedMode = mode;
    }

    private static void sendWeaponModeToServer(PaxelWeaponMode mode) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            lastSyncedWeaponMode = null;
            return;
        }

        ClientPacketDistributor.sendToServer(SetMainHandWeaponModePayload.fromMode(mode));
        lastSyncedWeaponMode = mode;
    }

    private static void syncHeldCombatMode(Minecraft minecraft) {
        Player player = minecraft.player;
        PaxelWeaponMode mode = ClientConfig.mainHandWeaponMode();
        PaxelMode mainHandToolMode = ClientConfig.mainHandMode();

        if (player.getMainHandItem().getItem() instanceof PaxelItem paxelItem) {
            paxelItem.applyWeaponMode(player.getMainHandItem(), player, mode);
            paxelItem.applyToolModeVisual(player.getMainHandItem(), mainHandToolMode);
        }

        if (player.getOffhandItem().getItem() instanceof PaxelItem paxelItem) {
            paxelItem.applyWeaponMode(player.getOffhandItem(), player, mode);
            paxelItem.applyToolModeVisual(player.getOffhandItem(), mainHandToolMode.opposite());
        }
    }

    private static void updateHandNotification(Minecraft minecraft, InteractionHand hand, PaxelMode mode) {
        Player player = minecraft.player;
        boolean hasPaxel = player.getItemInHand(hand).getItem() instanceof PaxelItem;

        if (hand == InteractionHand.MAIN_HAND) {
            if (hasPaxel && (mode != lastShownMainHandMode || !mainHandHadPaxel) && suppressOverlayTicks == 0) {
                showOverlay(minecraft, hand, mode);
            }
            mainHandHadPaxel = hasPaxel;
            lastShownMainHandMode = hasPaxel ? mode : null;
            return;
        }

        if (hasPaxel && (mode != lastShownOffHandMode || !offHandHadPaxel) && suppressOverlayTicks == 0) {
            showOverlay(minecraft, hand, mode);
        }
        offHandHadPaxel = hasPaxel;
        lastShownOffHandMode = hasPaxel ? mode : null;
    }

    private static void showOverlay(Minecraft minecraft, InteractionHand hand, PaxelMode mode) {
        minecraft.gui.setOverlayMessage(
                Component.translatable(
                        "message.hoepaxel.active_mode",
                        hand == InteractionHand.MAIN_HAND
                                ? Component.translatable("hand.hoepaxel.main_hand")
                                : Component.translatable("hand.hoepaxel.off_hand"),
                        mode.displayName()
                ),
                false
        );
    }

    private static void resetClientState() {
        lastSyncedMode = null;
        lastSyncedWeaponMode = null;
        lastShownMainHandMode = null;
        lastShownOffHandMode = null;
        mainHandHadPaxel = false;
        offHandHadPaxel = false;
        suppressOverlayTicks = 0;
    }
}
