package com.lyquidqrystal.gffm.mixins;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import com.lyquidqrystal.gffm.utils.ClientUtil;
import com.lyquidqrystal.gffm.utils.MelodiesUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin extends Mob {
    @Shadow
    public abstract Pokemon getPokemon();

    @Shadow
    public abstract void cry();

    @Unique
    private static final EntityDataAccessor<Integer> ENJOYING_MUSIC_STATE;

    static {
        ENJOYING_MUSIC_STATE = SynchedEntityData.defineId(PokemonEntityMixin.class, EntityDataSerializers.INT);
    }

    protected PokemonEntityMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private boolean gain_friendship_from_melodies$shouldCheckSoundProof() {
        return true;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickInject(CallbackInfo ci) {
        int friendshipValue = 2;//todo replace it with a value in the config
        if (this.getPokemon().isPlayerOwned()) {
            Player player = this.getPokemon().getOwnerPlayer();//The game crashes if the mixin extends the TamableAnimal class, so it might be the easiest way to get the owner.
            if (getPokemon().getAbility().getName().equals("soundproof") && gain_friendship_from_melodies$shouldCheckSoundProof()) {
                gain_friendship_from_melodies$setMusicState(-1);
            } else {
                long progress = MelodiesUtil.getProgress(player);
                long length = MelodiesUtil.getLength(player);
                if (progress < length) {//idk why the progress continues after the song is finished.
                    int progressToSec = Mth.floor((float) (progress + 1) / 100);

                    if (progressToSec % 50 == 0 && progressToSec > gain_friendship_from_melodies$getMusicState() && progress > 0) {
                        getPokemon().incrementFriendship(friendshipValue, true);
                        ClientUtil.makeParticle(5, this, ParticleTypes.HAPPY_VILLAGER);
                        gain_friendship_from_melodies$setMusicState(progressToSec + 1);
                        GainFriendshipFromMelodies.LOGGER.info(Long.toString(length));
                        GainFriendshipFromMelodies.LOGGER.info(Long.toString(progress));
                        GainFriendshipFromMelodies.LOGGER.info(Integer.toString(progressToSec));
                    }
                    if (length - progress < 5000 && progress > 0) {
                        if (gain_friendship_from_melodies$getMusicState() == 0) {
                            gain_friendship_from_melodies$setMusicState(gain_friendship_from_melodies$getMusicState() + 5);
                            cry();
                        }
                    } else {
                        gain_friendship_from_melodies$setMusicState(0);
                    }
                }
            }
        } else {
            gain_friendship_from_melodies$setMusicState(-1);
        }
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void defineMoreData(CallbackInfo ci) {
        entityData.define(ENJOYING_MUSIC_STATE, -1);
    }

    @Unique
    private int gain_friendship_from_melodies$getMusicState() {
        return entityData.get(ENJOYING_MUSIC_STATE);
    }

    @Unique
    private void gain_friendship_from_melodies$setMusicState(int value) {
        entityData.set(ENJOYING_MUSIC_STATE, value);
    }
}
