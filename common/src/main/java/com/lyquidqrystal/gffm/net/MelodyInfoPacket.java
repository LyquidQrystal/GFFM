package com.lyquidqrystal.gffm.net;

import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class MelodyInfoPacket implements NetworkPacket {
    public static final ResourceLocation MELODY_INFO_PACKET_ID = new ResourceLocation(GainFriendshipFromMelodies.MOD_ID, "melody_info");

    protected long progress;
    protected long length;

    public long getProgress() {
        return progress;
    }

    public long getLength() {
        return length;
    }

    public MelodyInfoPacket(long pro, long len) {
        progress = pro;
        length = len;
    }

    public MelodyInfoPacket(FriendlyByteBuf buf) {
        decode(buf);
    }


    @Override
    public FriendlyByteBuf encode() {
        var buf = Unpooled.buffer();
        buf.writeLong(progress);
        buf.writeLong(length);
        return new FriendlyByteBuf(buf);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        progress = buf.readLong();
        length = buf.readLong();
    }
}
