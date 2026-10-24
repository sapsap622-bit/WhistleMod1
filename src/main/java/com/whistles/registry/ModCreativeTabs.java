package com.whistles.registry;

import com.whistles.Whistles;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Whistles.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.whistles"))
                    .icon(() -> new ItemStack(ModItems.FREQUENCY_WHISTLE.get()))
                    .displayItems((parameters, output) -> output.accept(ModItems.FREQUENCY_WHISTLE.get()))
                    .build());

    private ModCreativeTabs() {}
}
