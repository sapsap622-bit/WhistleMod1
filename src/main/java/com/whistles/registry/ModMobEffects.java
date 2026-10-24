package com.whistles.registry;

import com.whistles.Whistles;
import com.whistles.effect.StasisEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Whistles.MODID);

    public static final DeferredHolder<MobEffect, StasisEffect> STASIS =
            EFFECTS.register("stasis", StasisEffect::new);

    private ModMobEffects() {}
}
