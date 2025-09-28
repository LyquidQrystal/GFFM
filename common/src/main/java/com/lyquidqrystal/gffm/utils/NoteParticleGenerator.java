package com.lyquidqrystal.gffm.utils;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import net.minecraft.core.particles.ParticleTypes;
import org.joml.Vector3f;

public class NoteParticleGenerator {
    protected PokemonEntity pokemon;
    protected long lastTimeCreatingNote = -1;

    public NoteParticleGenerator(PokemonEntity pokemon) {
        this.pokemon = pokemon;
    }

    public void onNewNote(long time, Vector3f offset, double x, double z) {
        if (lastTimeCreatingNote > time) {
            lastTimeCreatingNote = -1;
        }
        if (lastTimeCreatingNote < 0 || time - lastTimeCreatingNote > getInterval() * 50L) {
            createParticle(offset, x, z);
            lastTimeCreatingNote = time;
        }
    }

    protected void createParticle(Vector3f offset, double x, double z) {
        pokemon.level().addParticle(ParticleTypes.NOTE, pokemon.getX() + x * (double) offset.z + z * (double) offset.x, pokemon.getY() + (double) pokemon.getBbHeight() / 2.0 + (double) offset.y, pokemon.getZ() + z * (double) offset.z - x * (double) offset.x, x * 5.0, 0.0, z * 5.0);
    }

    protected int getInterval() {
        return GainFriendshipFromMelodies.commonConfig().minimum_interval_between_notes;
    }
}
