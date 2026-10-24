package com.whistles.registry;

import com.whistles.Whistles;
import com.whistles.item.FrequencyWhistleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Whistles.MODID);

    public static final DeferredItem<FrequencyWhistleItem> FREQUENCY_WHISTLE = ITEMS.register(
            "frequency_whistle",
            () -> new FrequencyWhistleItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    private ModItems() {}
}
