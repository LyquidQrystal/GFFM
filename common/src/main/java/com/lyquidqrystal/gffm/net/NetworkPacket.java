package com.lyquidqrystal.gffm.net;

import net.minecraft.network.FriendlyByteBuf;

public interface NetworkPacket {
    FriendlyByteBuf encode();

    void decode(FriendlyByteBuf buf);


}
