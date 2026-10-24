package com.whistles;

import com.mojang.logging.LogUtils;
import com.whistles.network.ModNetwork;
import com.whistles.registry.ModCreativeTabs;
import com.whistles.registry.ModItems;
import com.whistles.registry.ModMobEffects;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Whistles.MODID)
public class Whistles {
    public static final String MODID = "whistles";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Whistles(IEventBus modEventBus) {
        ModMobEffects.EFFECTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        modEventBus.addListener(ModNetwork::register);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
