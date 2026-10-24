package com.whistles.network;

import com.whistles.client.ClientPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    private ModNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(ShockwavePayload.TYPE, ShockwavePayload.STREAM_CODEC, ClientPayloadHandler::onShockwave);
        registrar.playToClient(ChannelPayload.TYPE, ChannelPayload.STREAM_CODEC, ClientPayloadHandler::onChannel);
    }
}
