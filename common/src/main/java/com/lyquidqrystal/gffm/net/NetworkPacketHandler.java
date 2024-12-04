package com.lyquidqrystal.gffm.net;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

public interface NetworkPacketHandler<T extends NetworkPacket> {
    void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context);

    T getPacket(FriendlyByteBuf buf);
}
