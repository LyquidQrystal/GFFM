package com.lyquidqrystal.gffm.net.handler;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import com.lyquidqrystal.gffm.interfaces.PokemonEntityInterface;
import com.lyquidqrystal.gffm.net.NetworkPacketHandler;
import com.lyquidqrystal.gffm.net.packet.MelodyInfoPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class MelodyInfoPacketHandler implements NetworkPacketHandler<MelodyInfoPacket> {
    @Override
    public void handle(MelodyInfoPacket packet, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        int d = GainFriendshipFromMelodies.commonConfig().distance_limit * 2;
        List<PokemonEntity> entities = player.level().getEntitiesOfClass(PokemonEntity.class, AABB.ofSize(player.position(), d, d, d));
        for (PokemonEntity pokemon : entities) {
            if (pokemon.getOwner() instanceof Player owner) {
                if (owner.equals(player)) {
                    ((PokemonEntityInterface) pokemon).setMusicLength(packet.getLength());
                    ((PokemonEntityInterface) pokemon).setMusicProgress(packet.getProgress());
                }
            }
        }
    }

}
