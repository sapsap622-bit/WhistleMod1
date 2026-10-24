package com.whistles.network;

import com.whistles.Whistles;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -> client: a shockwave happened at (x, y, z). {@code pulse} 0 is the initial blast when the whistle starts
 * sounding, 1..n are the shockwaves that apply Stasis.
 */
public record ShockwavePayload(double x, double y, double z, float radius, int pulse) implements CustomPacketPayload {
    public static final Type<ShockwavePayload> TYPE = new Type<>(Whistles.id("shockwave"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShockwavePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ShockwavePayload::x,
            ByteBufCodecs.DOUBLE, ShockwavePayload::y,
            ByteBufCodecs.DOUBLE, ShockwavePayload::z,
            ByteBufCodecs.FLOAT, ShockwavePayload::radius,
            ByteBufCodecs.VAR_INT, ShockwavePayload::pulse,
            ShockwavePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
