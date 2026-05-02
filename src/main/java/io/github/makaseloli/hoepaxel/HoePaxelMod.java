package io.github.makaseloli.hoepaxel;

import io.github.makaseloli.hoepaxel.network.SetMainHandModePayload;
import io.github.makaseloli.hoepaxel.network.SetMainHandWeaponModePayload;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(HoePaxelMod.MOD_ID)
public final class HoePaxelMod {
    public static final String MOD_ID = "hoepaxel";

    public HoePaxelMod(IEventBus modBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modBus);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        modBus.addListener(HoePaxelMod::buildCreativeTab);
        modBus.addListener(HoePaxelMod::registerPayloadHandlers);
    }

    private static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            ModItems.all().forEach(item -> event.accept(item.get()));
        }
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(SetMainHandModePayload.TYPE, SetMainHandModePayload.STREAM_CODEC, SetMainHandModePayload::handle)
                .playToServer(SetMainHandWeaponModePayload.TYPE, SetMainHandWeaponModePayload.STREAM_CODEC, SetMainHandWeaponModePayload::handle);
    }
}
