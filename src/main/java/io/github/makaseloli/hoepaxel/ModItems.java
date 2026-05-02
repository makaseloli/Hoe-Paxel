package io.github.makaseloli.hoepaxel;

import java.util.List;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HoePaxelMod.MOD_ID);

    public static final DeferredItem<PaxelItem> WOODEN_PAXEL = register("wooden_paxel", ToolMaterial.WOOD);
    public static final DeferredItem<PaxelItem> STONE_PAXEL = register("stone_paxel", ToolMaterial.STONE);
    public static final DeferredItem<PaxelItem> COPPER_PAXEL = register("copper_paxel", ToolMaterial.COPPER);
    public static final DeferredItem<PaxelItem> IRON_PAXEL = register("iron_paxel", ToolMaterial.IRON);
    public static final DeferredItem<PaxelItem> GOLDEN_PAXEL = register("golden_paxel", ToolMaterial.GOLD);
    public static final DeferredItem<PaxelItem> DIAMOND_PAXEL = register("diamond_paxel", ToolMaterial.DIAMOND);
    public static final DeferredItem<PaxelItem> NETHERITE_PAXEL = register("netherite_paxel", ToolMaterial.NETHERITE);

    private ModItems() {
    }

    private static DeferredItem<PaxelItem> register(String name, ToolMaterial material) {
        return ITEMS.registerItem(name, properties -> new PaxelItem(material, properties));
    }

    public static List<DeferredItem<PaxelItem>> all() {
        return List.of(
                WOODEN_PAXEL,
                STONE_PAXEL,
                COPPER_PAXEL,
                IRON_PAXEL,
                GOLDEN_PAXEL,
                DIAMOND_PAXEL,
                NETHERITE_PAXEL
        );
    }
}

