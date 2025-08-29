package com.lyquidqrystal.gffm.net.packet;

import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import com.lyquidqrystal.gffm.net.NetworkPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MelodyInfoPacket implements NetworkPacket, CustomPacketPayload {
    public static final ResourceLocation MELODY_INFO_PACKET_ID = ResourceLocation.fromNamespaceAndPath(GainFriendshipFromMelodies.MOD_ID, "melody_info");
    public static final StreamCodec<ByteBuf, MelodyInfoPacket> STREAM_CODEC;
    public static final Type<MelodyInfoPacket> TYPE = new Type<>(MELODY_INFO_PACKET_ID);

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

    static {
        STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG, MelodyInfoPacket::getProgress,
                ByteBufCodecs.VAR_LONG, MelodyInfoPacket::getLength,
                MelodyInfoPacket::new);
    }
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
