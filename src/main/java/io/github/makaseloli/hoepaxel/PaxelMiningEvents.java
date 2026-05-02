package io.github.makaseloli.hoepaxel;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = HoePaxelMod.MOD_ID)
public final class PaxelMiningEvents {
    private PaxelMiningEvents() {
    }

    @SubscribeEvent
    public static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        ItemStack proxyPaxel = getProxyOffhandPaxel(event.getEntity());
        if (proxyPaxel.isEmpty()) {
            return;
        }

        if (!event.canHarvest() && proxyPaxel.isCorrectToolForDrops(event.getTargetBlock())) {
            event.setCanHarvest(true);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack proxyPaxel = getProxyOffhandPaxel(event.getEntity());
        if (proxyPaxel.isEmpty()) {
            return;
        }

        float proxySpeed = proxyPaxel.getDestroySpeed(event.getState());
        if (proxySpeed > event.getNewSpeed()) {
            event.setNewSpeed(proxySpeed);
        }
    }

    private static ItemStack getProxyOffhandPaxel(Player player) {
        if (!player.getMainHandItem().isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack offhand = player.getOffhandItem();
        return offhand.getItem() instanceof PaxelItem ? offhand : ItemStack.EMPTY;
    }
}

