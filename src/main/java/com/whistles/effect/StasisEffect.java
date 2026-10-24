package com.whistles.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Stasis: the affected entity is frozen in place and cannot act.
 * The actual freezing logic lives in {@link StasisHandler}.
 */
public class StasisEffect extends MobEffect {
    public StasisEffect() {
        super(MobEffectCategory.HARMFUL, 0x7FD9FF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        StasisHandler.hold(entity);
        return true;
    }
}
