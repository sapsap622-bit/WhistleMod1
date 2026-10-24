package com.whistles.network;

import com.whistles.Whistles;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -> the player blowing the whistle: the forced 5 s channel started ({@code true}) or ended ({@code false}).
 * While active, the client keeps the "use item" key held so the animation can't be cancelled.
 */
public record ChannelPayload(boolean active) implements CustomPacketPayload {
    public static final Type<ChannelPayload> TYPE = new Type<>(Whistles.id("channel"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ChannelPayload::active,
            ChannelPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
