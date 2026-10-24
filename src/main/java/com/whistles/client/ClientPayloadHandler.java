package com.whistles.client;

import com.whistles.network.ChannelPayload;
import com.whistles.network.ShockwavePayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client-side packet handlers (run on the main thread). Only ever referenced from the client. */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void onShockwave(ShockwavePayload payload, IPayloadContext context) {
        WhistleClient.onShockwave(payload);
    }

    public static void onChannel(ChannelPayload payload, IPayloadContext context) {
        WhistleClient.setChannelActive(payload.active());
    }
}
