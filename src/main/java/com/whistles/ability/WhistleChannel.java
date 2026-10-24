package com.whistles.ability;

import com.whistles.WhistleConfig;
import com.whistles.Whistles;
import com.whistles.item.FrequencyWhistleItem;
import com.whistles.network.ChannelPayload;
import com.whistles.network.ShockwavePayload;
import com.whistles.registry.ModItems;
import com.whistles.registry.ModMobEffects;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side state of The Frequency Whistle while it is being blown.
 * <p>
 * The 5 s channel is driven entirely from here, not from the vanilla "use item" timer, so letting go of right click,
 * changing hotbar slot or opening the inventory cannot cancel it: the server keeps re-starting the use animation and
 * puts the player back on the whistle's slot until the channel is over.
 */
@EventBusSubscriber(modid = Whistles.MODID)
public final class WhistleChannel {
    /** Placeholder sound: the first vanilla goat horn variant. */
    private static final SoundEvent HORN_SOUND =
            SoundEvent.createVariableRangeEvent(ResourceLocation.withDefaultNamespace("item.goat_horn.sound.0"));

    private static final Map<UUID, State> ACTIVE = new HashMap<>();

    private static final class State {
        final InteractionHand hand;
        final int slot;
        int elapsed;

        State(InteractionHand hand, int slot) {
            this.hand = hand;
            this.slot = slot;
        }
    }

    private WhistleChannel() {}

    public static boolean isChanneling(ServerPlayer player) {
        return ACTIVE.containsKey(player.getUUID());
    }

    // ---------------------------------------------------------------- start / tick / finish

    public static void start(ServerPlayer player, InteractionHand hand) {
        ACTIVE.put(player.getUUID(), new State(hand, player.getInventory().selected));

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                HORN_SOUND, SoundSource.PLAYERS, 3.0F, 1.0F);

        PacketDistributor.sendToPlayer(player, new ChannelPayload(true));
        broadcastShockwave(level, player.position(), 0); // initial blast: camera effect, no Stasis yet
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        State state = ACTIVE.get(player.getUUID());
        if (state == null) return;

        if (!player.isAlive()) {
            cancel(player);
            return;
        }

        keepBlowing(player, state);

        state.elapsed++;
        if (state.elapsed < WhistleConfig.CHANNEL_TICKS && state.elapsed % WhistleConfig.PULSE_INTERVAL_TICKS == 0) {
            pulse(player, state.elapsed / WhistleConfig.PULSE_INTERVAL_TICKS);
        }
        if (state.elapsed >= WhistleConfig.CHANNEL_TICKS) {
            finish(player);
        }
    }

    /** Makes the use animation impossible to cancel. */
    private static void keepBlowing(ServerPlayer player, State state) {
        if (state.hand == InteractionHand.MAIN_HAND && player.getInventory().selected != state.slot) {
            player.getInventory().selected = state.slot;
            player.connection.send(new ClientboundSetCarriedItemPacket(state.slot));
        }
        ItemStack held = player.getItemInHand(state.hand);
        if (held.getItem() instanceof FrequencyWhistleItem && !player.isUsingItem()) {
            player.startUsingItem(state.hand);
        }
    }

    private static void finish(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof FrequencyWhistleItem) {
            player.stopUsingItem();
        }
        player.getCooldowns().addCooldown(ModItems.FREQUENCY_WHISTLE.get(), WhistleConfig.COOLDOWN_TICKS);
        PacketDistributor.sendToPlayer(player, new ChannelPayload(false));
    }

    private static void cancel(ServerPlayer player) {
        if (ACTIVE.remove(player.getUUID()) != null) {
            PacketDistributor.sendToPlayer(player, new ChannelPayload(false));
        }
    }

    // ---------------------------------------------------------------- shockwave

    private static void pulse(ServerPlayer caster, int pulseIndex) {
        ServerLevel level = caster.serverLevel();
        Vec3 center = caster.position();
        double radius = WhistleConfig.RADIUS;
        AABB area = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (target == caster && !WhistleConfig.AFFECTS_CASTER) continue;
            if (!target.isAlive() || target.isSpectator()) continue;
            if (distanceSqToBox(target.getBoundingBox(), center) > radius * radius) continue;
            addStasis(target, WhistleConfig.STASIS_PER_PULSE_TICKS);
        }

        level.playSound(null, center.x, center.y, center.z, SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS, 4.0F, 0.5F + 0.1F * pulseIndex);
        broadcastShockwave(level, center, pulseIndex);
    }

    /** Adds time on top of whatever Stasis the entity still has left. */
    private static void addStasis(LivingEntity target, int addedTicks) {
        MobEffectInstance current = target.getEffect(ModMobEffects.STASIS);
        int remaining = current == null ? 0 : current.getDuration();
        target.addEffect(new MobEffectInstance(ModMobEffects.STASIS, remaining + addedTicks, 0, false, true, true));
    }

    private static double distanceSqToBox(AABB box, Vec3 point) {
        double dx = Math.max(Math.max(box.minX - point.x, point.x - box.maxX), 0.0);
        double dy = Math.max(Math.max(box.minY - point.y, point.y - box.maxY), 0.0);
        double dz = Math.max(Math.max(box.minZ - point.z, point.z - box.maxZ), 0.0);
        return dx * dx + dy * dy + dz * dz;
    }

    private static void broadcastShockwave(ServerLevel level, Vec3 center, int pulseIndex) {
        ShockwavePayload payload = new ShockwavePayload(center.x, center.y, center.z,
                (float) WhistleConfig.RADIUS, pulseIndex);
        double rangeSq = WhistleConfig.VISUAL_RANGE * WhistleConfig.VISUAL_RANGE;
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(center) <= rangeSq) {
                PacketDistributor.sendToPlayer(viewer, payload);
            }
        }
    }

    // ---------------------------------------------------------------- cleanup

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancel(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ACTIVE.clear();
    }
}
