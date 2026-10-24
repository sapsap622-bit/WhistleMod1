package com.whistles.client;

import com.whistles.Whistles;
import com.whistles.registry.ModMobEffects;
import net.minecraft.client.player.Input;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Game-bus events that only exist on the client. */
@EventBusSubscriber(modid = Whistles.MODID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        WhistleClient.enforceChannelHold();
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        WhistleClient.tick();
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        WhistleClient.applyCameraShake(event);
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        WhistleClient.applyFovPunch(event);
    }

    /** Stasis: a frozen player can't walk, strafe or jump. */
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (event.getEntity().hasEffect(ModMobEffects.STASIS)) {
            Input input = event.getInput();
            input.leftImpulse = 0.0F;
            input.forwardImpulse = 0.0F;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.jumping = false;
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        WhistleClient.reset();
    }
}
