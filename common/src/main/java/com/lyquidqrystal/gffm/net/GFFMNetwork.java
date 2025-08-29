package com.lyquidqrystal.gffm.net;

import com.lyquidqrystal.gffm.net.handler.MelodyInfoPacketHandler;
import com.lyquidqrystal.gffm.net.packet.MelodyInfoPacket;
import dev.architectury.networking.NetworkManager;

public class GFFMNetwork {
    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, MelodyInfoPacket.TYPE, MelodyInfoPacket.STREAM_CODEC, (packet, context) -> {
            MelodyInfoPacketHandler handler = new MelodyInfoPacketHandler();
            handler.handle(packet, context);
        });
    }
}
