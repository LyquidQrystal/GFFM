package com.lyquidqrystal.gffm.net;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import com.lyquidqrystal.gffm.interfaces.PokemonEntityInterface;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Objects;

public class MelodyInfoPacketHandler implements NetworkPacketHandler<MelodyInfoPacket> {
    @Override
    public void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        MelodyInfoPacket packet = getPacket(buf);
        Player player = context.getPlayer();
        int d = GainFriendshipFromMelodies.commonConfig().distance_limit * 2;
        List<PokemonEntity> entities = player.level().getEntitiesOfClass(PokemonEntity.class, AABB.ofSize(player.position(), d, d, d));
        for (PokemonEntity pokemon : entities) {
            ((PokemonEntityInterface) (Object) pokemon).setMusicLength(packet.getLength());
            ((PokemonEntityInterface) (Object) pokemon).setMusicProgress(packet.getProgress());
        }
    }

    @Override
    public MelodyInfoPacket getPacket(FriendlyByteBuf buf) {
        return new MelodyInfoPacket(buf);
    }
}
