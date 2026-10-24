package com.whistles.effect;

import com.whistles.Whistles;
import com.whistles.registry.ModMobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Everything that makes Stasis actually freeze things.
 * <ul>
 *   <li>Mobs: AI is switched off while the effect lasts (no movement, no gravity, no attacks) and restored afterwards.</li>
 *   <li>Players: movement input is zeroed on the client (see ClientEvents) and interactions are cancelled here.</li>
 * </ul>
 */
@EventBusSubscriber(modid = Whistles.MODID)
public final class StasisHandler {
    private static final String PREV_NO_AI_KEY = "whistles_stasis_prev_noai";

    private StasisHandler() {}

    /** Called every tick (server side) by the effect on the frozen entity. */
    public static void hold(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            CompoundTag data = mob.getPersistentData();
            if (!data.contains(PREV_NO_AI_KEY)) {
                data.putBoolean(PREV_NO_AI_KEY, mob.isNoAi());
                mob.setNoAi(true);
            }
            mob.setDeltaMovement(Vec3.ZERO);
        }
    }

    /** Restores the mob's original AI state once Stasis is gone. */
    public static void release(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            CompoundTag data = mob.getPersistentData();
            if (data.contains(PREV_NO_AI_KEY)) {
                boolean hadNoAi = data.getBoolean(PREV_NO_AI_KEY);
                data.remove(PREV_NO_AI_KEY);
                mob.setNoAi(hadNoAi);
            }
        }
    }

    private static boolean isFrozen(Entity entity) {
        return entity instanceof LivingEntity living && living.hasEffect(ModMobEffects.STASIS);
    }

    // ---------------------------------------------------------------- effect lifecycle

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect().value() == ModMobEffects.STASIS.get()) {
            release(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect().value() == ModMobEffects.STASIS.get()) {
            release(event.getEntity());
        }
    }

    /** Safety net: if the effect vanished in some way that fired no event, thaw the mob anyway. */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob mob
                && !mob.level().isClientSide
                && mob.getPersistentData().contains(PREV_NO_AI_KEY)
                && !mob.hasEffect(ModMobEffects.STASIS)) {
            release(mob);
        }
    }

    // ---------------------------------------------------------------- player restrictions

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (isFrozen(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isFrozen(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isFrozen(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (isFrozen(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isFrozen(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (isFrozen(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (isFrozen(event.getPlayer())) event.setCanceled(true);
    }
}
