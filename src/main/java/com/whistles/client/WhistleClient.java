package com.whistles.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.whistles.network.ShockwavePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Client-side state for The Frequency Whistle: ground shockwave rings, camera shake / FOV punch and the
 * "keep right click held" enforcement that makes the blowing animation uncancellable.
 */
public final class WhistleClient {
    private static final int RING_LIFE_TICKS = 10;
    private static final DustParticleOptions DUST = new DustParticleOptions(new Vector3f(0.45F, 0.85F, 1.0F), 1.3F);

    private record Ring(double x, double y, double z, float radius, int pulse, int startTick) {}

    private static final List<Ring> RINGS = new ArrayList<>();
    private static int clientTick;
    private static boolean channelActive;

    private static float shake;
    private static float prevShake;
    private static float fovKick; // negative = narrower FOV
    private static float prevFovKick;

    private WhistleClient() {}

    // ---------------------------------------------------------------- packets

    public static void setChannelActive(boolean active) {
        channelActive = active;
        if (!active) {
            // hand the key back to whatever the physical button is doing right now
            KeyMapping use = Minecraft.getInstance().options.keyUse;
            use.setDown(isPhysicallyDown(use));
        }
    }

    public static void onShockwave(ShockwavePayload payload) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        RINGS.add(new Ring(payload.x(), payload.y(), payload.z(), payload.radius(), payload.pulse(), clientTick));

        if (payload.pulse() == 0) {
            RandomSource random = level.random;
            for (int i = 0; i < 24; i++) {
                level.addParticle(ParticleTypes.END_ROD, payload.x(), payload.y() + 1.0, payload.z(),
                        random.nextGaussian() * 0.15, random.nextGaussian() * 0.15, random.nextGaussian() * 0.15);
            }
        }

        LocalPlayer player = mc.player;
        if (player == null) return;
        double distance = player.position().distanceTo(new Vec3(payload.x(), payload.y(), payload.z()));
        float falloff = (float) Mth.clamp(1.0 - distance / (payload.radius() * 4.0), 0.0, 1.0);
        if (falloff <= 0.0F) return;

        float strength = (payload.pulse() == 0 ? 1.0F : 0.65F) * falloff;
        shake = Math.max(shake, strength);
        fovKick = Math.min(fovKick, -0.10F * strength);
    }

    // ---------------------------------------------------------------- ticking

    /** Runs at the start of every client tick, before vanilla reads the key bindings. */
    public static void enforceChannelHold() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            channelActive = false;
            return;
        }
        if (channelActive) {
            mc.options.keyUse.setDown(true);
        }
    }

    public static void tick() {
        clientTick++;
        prevShake = shake;
        prevFovKick = fovKick;
        shake *= 0.88F;
        if (shake < 0.01F) shake = 0.0F;
        fovKick *= 0.78F;
        if (fovKick > -0.002F) fovKick = 0.0F;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            RINGS.clear();
            return;
        }

        Iterator<Ring> iterator = RINGS.iterator();
        while (iterator.hasNext()) {
            Ring ring = iterator.next();
            int age = clientTick - ring.startTick();
            if (age >= RING_LIFE_TICKS) {
                iterator.remove();
            } else {
                spawnRingParticles(level, ring, age);
            }
        }
    }

    public static void reset() {
        RINGS.clear();
        channelActive = false;
        shake = prevShake = 0.0F;
        fovKick = prevFovKick = 0.0F;
    }

    // ---------------------------------------------------------------- camera

    public static void applyCameraShake(ViewportEvent.ComputeCameraAngles event) {
        float partial = (float) event.getPartialTick();
        float current = Mth.lerp(partial, prevShake, shake);
        if (current <= 0.0F) return;

        float amplitude = current * current;
        double time = clientTick + partial;
        event.setYaw(event.getYaw() + (float) (Math.sin(time * 2.1) * amplitude * 2.5));
        event.setPitch(event.getPitch() + (float) (Math.sin(time * 2.9 + 1.3) * amplitude * 2.0));
        event.setRoll(event.getRoll() + (float) (Math.sin(time * 3.3 + 2.1) * amplitude * 3.5));
    }

    public static void applyFovPunch(ViewportEvent.ComputeFov event) {
        float kick = Mth.lerp((float) event.getPartialTick(), prevFovKick, fovKick);
        if (kick != 0.0F) {
            event.setFOV(event.getFOV() * (1.0 + kick));
        }
    }

    // ---------------------------------------------------------------- ground shockwave

    private static void spawnRingParticles(ClientLevel level, Ring ring, int age) {
        float progress = (age + 1) / (float) RING_LIFE_TICKS;
        double eased = 1.0 - Math.pow(1.0 - progress, 2.0); // fast at first, slows down
        double radius = ring.radius() * eased;
        int points = Math.max(12, (int) (radius * 6.0));
        RandomSource random = level.random;
        double offset = random.nextDouble() * Math.PI * 2.0;

        for (int i = 0; i < points; i++) {
            double angle = offset + (Math.PI * 2.0 * i) / points;
            double px = ring.x() + Math.cos(angle) * radius;
            double pz = ring.z() + Math.sin(angle) * radius;
            double groundY = findGroundY(level, px, pz, ring.y());
            if (Double.isNaN(groundY)) continue;

            level.addParticle(DUST, px, groundY + 0.08, pz, 0.0, 0.02, 0.0);

            if ((i & 1) == 0) {
                BlockState ground = level.getBlockState(BlockPos.containing(px, groundY - 0.1, pz));
                if (!ground.isAir()) {
                    level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                            px, groundY + 0.1, pz, 0.0, 0.18 + random.nextDouble() * 0.12, 0.0);
                }
            }
            if (i % 5 == 0) {
                level.addParticle(ParticleTypes.END_ROD, px, groundY + 0.25, pz, 0.0, 0.03, 0.0);
            }
        }
    }

    /** Finds the top of the first solid block (with free space above it) below the caster's head height. */
    private static double findGroundY(ClientLevel level, double x, double z, double referenceY) {
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        int startY = Mth.floor(referenceY) + 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = startY; y >= startY - 6; y--) {
            pos.set(bx, y, bz);
            VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
            if (shape.isEmpty()) continue;

            BlockPos above = new BlockPos(bx, y + 1, bz);
            if (level.getBlockState(above).getCollisionShape(level, above).isEmpty()) {
                return y + shape.max(Direction.Axis.Y);
            }
        }
        return Double.NaN;
    }

    // ---------------------------------------------------------------- input helper

    private static boolean isPhysicallyDown(KeyMapping mapping) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        InputConstants.Key key = mapping.getKey();
        return switch (key.getType()) {
            case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
            default -> false;
        };
    }
}
