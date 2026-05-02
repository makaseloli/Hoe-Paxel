package io.github.makaseloli.hoepaxel;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.extensions.IBlockExtension;

public final class PaxelItem extends Item {
    private static final String TOOL_MODE_NONE = "none";

    private final ToolMaterial material;

    public PaxelItem(ToolMaterial material, Item.Properties properties) {
        super(configureProperties(material, properties));
        this.material = material;
    }

    private static Item.Properties configureProperties(ToolMaterial material, Item.Properties properties) {
        applySpearProperties(material, properties);
        properties.component(DataComponents.TOOL, createCombinedTool(material));
        properties.component(DataComponents.WEAPON, new Weapon(1, Weapon.AXE_DISABLES_BLOCKING_FOR_SECONDS));

        if (material == ToolMaterial.NETHERITE) {
            properties.fireResistant();
        }

        return properties;
    }

    private static void applySpearProperties(ToolMaterial material, Item.Properties properties) {
        if (material == ToolMaterial.WOOD) {
            properties.spear(material, 0.65F, 0.7F, 0.75F, 5.0F, 14.0F, 10.0F, 5.1F, 15.0F, 4.6F);
        } else if (material == ToolMaterial.STONE) {
            properties.spear(material, 0.75F, 0.82F, 0.7F, 4.5F, 13.0F, 9.0F, 5.1F, 13.75F, 4.6F);
        } else if (material == ToolMaterial.COPPER) {
            properties.spear(material, 0.85F, 0.82F, 0.65F, 4.0F, 12.0F, 8.25F, 5.1F, 12.5F, 4.6F);
        } else if (material == ToolMaterial.IRON) {
            properties.spear(material, 0.95F, 0.95F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F);
        } else if (material == ToolMaterial.GOLD) {
            properties.spear(material, 0.95F, 0.7F, 0.7F, 3.5F, 13.0F, 8.5F, 5.1F, 13.75F, 4.6F);
        } else if (material == ToolMaterial.DIAMOND) {
            properties.spear(material, 1.05F, 1.075F, 0.5F, 3.0F, 10.0F, 6.5F, 5.1F, 10.0F, 4.6F);
        } else if (material == ToolMaterial.NETHERITE) {
            properties.spear(material, 1.15F, 1.2F, 0.4F, 2.5F, 9.0F, 5.5F, 5.1F, 8.75F, 4.6F);
        }
    }

    private static Tool createCombinedTool(ToolMaterial material) {
        HolderGetter<net.minecraft.world.level.block.Block> blocks =
                BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);

        return new Tool(
                List.of(
                        Tool.Rule.deniesDrops(blocks.getOrThrow(material.incorrectBlocksForDrops())),
                        Tool.Rule.minesAndDrops(blocks.getOrThrow(BlockTags.MINEABLE_WITH_PICKAXE), material.speed()),
                        Tool.Rule.minesAndDrops(blocks.getOrThrow(BlockTags.MINEABLE_WITH_AXE), material.speed()),
                        Tool.Rule.minesAndDrops(blocks.getOrThrow(BlockTags.MINEABLE_WITH_SHOVEL), material.speed()),
                        Tool.Rule.minesAndDrops(blocks.getOrThrow(BlockTags.MINEABLE_WITH_HOE), material.speed()),
                        Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
                        Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_INSTANTLY_MINES), Float.MAX_VALUE),
                        Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)
                ),
                1.0F,
                1,
                true
        );
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        if (itemAbility == ItemAbilities.SWORD_SWEEP) {
            return !stack.has(DataComponents.PIERCING_WEAPON);
        }

        return itemAbility == ItemAbilities.AXE_STRIP
                || itemAbility == ItemAbilities.AXE_SCRAPE
                || itemAbility == ItemAbilities.AXE_WAX_OFF
                || itemAbility == ItemAbilities.SHOVEL_FLATTEN
                || itemAbility == ItemAbilities.SHOVEL_DOUSE
                || itemAbility == ItemAbilities.HOE_TILL;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, net.minecraft.server.level.ServerLevel level, Entity owner, EquipmentSlot slot) {
        if (owner instanceof Player player) {
            applyWeaponMode(itemStack, player, PaxelModeState.getMainHandWeaponMode(player));
            applyToolModeVisual(itemStack, resolveHeldToolMode(itemStack, player));
        }
    }

    public void applyWeaponMode(ItemStack stack, Player player, PaxelWeaponMode mode) {
        if (mode == PaxelWeaponMode.SPEAR) {
            if (!stack.has(DataComponents.KINETIC_WEAPON) || !stack.has(DataComponents.PIERCING_WEAPON)) {
                copyWeaponComponent(stack, DataComponents.KINETIC_WEAPON, vanillaSpearItem());
                copyWeaponComponent(stack, DataComponents.PIERCING_WEAPON, vanillaSpearItem());
                copyWeaponComponent(stack, DataComponents.ATTACK_RANGE, vanillaSpearItem());
                copyWeaponComponent(stack, DataComponents.MINIMUM_ATTACK_CHARGE, vanillaSpearItem());
                copyWeaponComponent(stack, DataComponents.SWING_ANIMATION, vanillaSpearItem());
                copyWeaponComponent(stack, DataComponents.USE_EFFECTS, vanillaSpearItem());
                copyWeaponComponent(stack, DataComponents.DAMAGE_TYPE, vanillaSpearItem());
                copyWeaponComponent(stack, DataComponents.ATTRIBUTE_MODIFIERS, vanillaSpearItem());
                copyWeaponComponent(stack, DataComponents.WEAPON, vanillaSpearItem());
            }
            return;
        }

        if (stack.has(DataComponents.KINETIC_WEAPON) || stack.has(DataComponents.PIERCING_WEAPON)) {
            stack.remove(DataComponents.KINETIC_WEAPON);
            stack.remove(DataComponents.PIERCING_WEAPON);
            stack.remove(DataComponents.ATTACK_RANGE);
            stack.remove(DataComponents.MINIMUM_ATTACK_CHARGE);
            stack.remove(DataComponents.SWING_ANIMATION);
            stack.remove(DataComponents.USE_EFFECTS);
            stack.remove(DataComponents.DAMAGE_TYPE);
            copyWeaponComponent(stack, DataComponents.ATTRIBUTE_MODIFIERS, vanillaSwordItem());
            copyWeaponComponent(stack, DataComponents.WEAPON, vanillaSwordItem());
        }
    }

    public void applyToolModeVisual(ItemStack stack, PaxelMode mode) {
        updateToolModeMarker(stack, mode != null ? mode.serializedName() : TOOL_MODE_NONE);
    }

    private PaxelMode resolveHeldToolMode(ItemStack stack, Player player) {
        if (player.getMainHandItem() == stack) {
            return PaxelModeState.getModeForHand(player, InteractionHand.MAIN_HAND);
        }
        if (player.getOffhandItem() == stack) {
            return PaxelModeState.getModeForHand(player, InteractionHand.OFF_HAND);
        }
        return null;
    }

    private void updateToolModeMarker(ItemStack stack, String toolMode) {
        CustomModelData existing = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        List<String> strings = new ArrayList<>(existing != null ? existing.strings() : List.of());
        while (strings.size() <= 0) {
            strings.add("");
        }

        if (toolMode.equals(strings.getFirst())) {
            return;
        }

        strings.set(0, toolMode);
        stack.set(
                DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(
                        existing != null ? existing.floats() : List.of(),
                        existing != null ? existing.flags() : List.of(),
                        List.copyOf(strings),
                        existing != null ? existing.colors() : List.of()
                )
        );
    }

    private <T> void copyWeaponComponent(ItemStack target, net.minecraft.core.component.DataComponentType<T> type, Item sourceItem) {
        target.copyFrom(type, sourceItem.components());
    }

    private Item vanillaSwordItem() {
        if (material == ToolMaterial.WOOD) {
            return Items.WOODEN_SWORD;
        } else if (material == ToolMaterial.STONE) {
            return Items.STONE_SWORD;
        } else if (material == ToolMaterial.COPPER) {
            return Items.COPPER_SWORD;
        } else if (material == ToolMaterial.IRON) {
            return Items.IRON_SWORD;
        } else if (material == ToolMaterial.GOLD) {
            return Items.GOLDEN_SWORD;
        } else if (material == ToolMaterial.DIAMOND) {
            return Items.DIAMOND_SWORD;
        }
        return Items.NETHERITE_SWORD;
    }

    private Item vanillaSpearItem() {
        if (material == ToolMaterial.WOOD) {
            return Items.WOODEN_SPEAR;
        } else if (material == ToolMaterial.STONE) {
            return Items.STONE_SPEAR;
        } else if (material == ToolMaterial.COPPER) {
            return Items.COPPER_SPEAR;
        } else if (material == ToolMaterial.IRON) {
            return Items.IRON_SPEAR;
        } else if (material == ToolMaterial.GOLD) {
            return Items.GOLDEN_SPEAR;
        } else if (material == ToolMaterial.DIAMOND) {
            return Items.DIAMOND_SPEAR;
        }
        return Items.NETHERITE_SPEAR;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (playerHasBlockingItemUseIntent(context)) {
            return InteractionResult.PASS;
        }

        InteractionResult axeResult = tryModifyBlock(context, ItemAbilities.AXE_STRIP);
        if (axeResult.consumesAction()) {
            return axeResult;
        }

        axeResult = tryModifyBlock(context, ItemAbilities.AXE_SCRAPE);
        if (axeResult.consumesAction()) {
            return axeResult;
        }

        axeResult = tryModifyBlock(context, ItemAbilities.AXE_WAX_OFF);
        if (axeResult.consumesAction()) {
            return axeResult;
        }

        Player player = context.getPlayer();
        PaxelMode mode = player != null ? PaxelModeState.getModeForHand(player, context.getHand()) : PaxelMode.SHOVEL;
        if (mode == PaxelMode.SHOVEL) {
            InteractionResult shovelResult = tryModifyBlock(context, ItemAbilities.SHOVEL_FLATTEN);
            if (shovelResult.consumesAction()) {
                return shovelResult;
            }

            return tryModifyBlock(context, ItemAbilities.SHOVEL_DOUSE);
        }

        return tryModifyBlock(context, ItemAbilities.HOE_TILL);
    }

    private static boolean playerHasBlockingItemUseIntent(UseOnContext context) {
        Player player = context.getPlayer();
        return player != null
                && context.getHand() == InteractionHand.MAIN_HAND
                && player.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS)
                && !player.isSecondaryUseActive();
    }

    private static InteractionResult tryModifyBlock(UseOnContext context, ItemAbility itemAbility) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState currentState = level.getBlockState(pos);
        BlockState modifiedState = ((IBlockExtension) currentState.getBlock()).getToolModifiedState(currentState, context, itemAbility, false);
        if (modifiedState == null) {
            return InteractionResult.PASS;
        }

        playToolFeedback(level, pos, context, currentState, itemAbility);

        if (!level.isClientSide()) {
            level.setBlock(pos, modifiedState, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(context.getPlayer(), modifiedState));

            Player player = context.getPlayer();
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static void playToolFeedback(Level level, BlockPos pos, UseOnContext context, BlockState currentState, ItemAbility itemAbility) {
        Player player = context.getPlayer();

        if (itemAbility == ItemAbilities.AXE_STRIP) {
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (itemAbility == ItemAbilities.AXE_SCRAPE) {
            level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, 3005, pos, 0);
        } else if (itemAbility == ItemAbilities.AXE_WAX_OFF) {
            level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, 3004, pos, 0);
        } else if (itemAbility == ItemAbilities.SHOVEL_FLATTEN) {
            level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (itemAbility == ItemAbilities.HOE_TILL) {
            level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (itemAbility == ItemAbilities.SHOVEL_DOUSE && !level.isClientSide()) {
            level.levelEvent(null, 1009, pos, 0);
        }
    }
}
