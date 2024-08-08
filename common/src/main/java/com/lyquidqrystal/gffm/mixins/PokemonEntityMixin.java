package com.lyquidqrystal.gffm.mixins;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import com.lyquidqrystal.gffm.utils.ClientUtil;
import com.lyquidqrystal.gffm.utils.MelodiesUtil;
import immersive_melodies.client.MelodyProgress;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin extends Mob {
    @Shadow
    public abstract Pokemon getPokemon();

    @Shadow
    public abstract void cry();

    @Shadow
    public abstract void playAmbientSound();

    @Shadow
    public abstract boolean isBattling();

    @Unique
    private int gain_friendship_from_melodies$musicState;
    @Unique
    private boolean gain_friendship_from_melodies$hasWarned;

    protected PokemonEntityMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        gain_friendship_from_melodies$hasWarned = false;
        gain_friendship_from_melodies$musicState = 0;
    }

    @Unique
    private boolean gain_friendship_from_melodies$shouldCheckSoundProof() {
        return GainFriendshipFromMelodies.commonConfig().is_soundproof_on;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickInject(CallbackInfo ci) {
        int friendshipValue = GainFriendshipFromMelodies.commonConfig().friendship_increment_value;
        long COOLDOWN = GainFriendshipFromMelodies.commonConfig().increase_friendship_cooldown;
        boolean isHearing = false;
        if (this.getPokemon().isPlayerOwned() && !isBattling()) {
            Player player = this.getPokemon().getOwnerPlayer();//The game crashes if the mixin extends the TamableAnimal class, so it might be the easiest way to get the owner.
            if (this.level().dimension() == player.level().dimension() && this.distanceTo(player) < GainFriendshipFromMelodies.commonConfig().distance_limit && !(getPokemon().getAbility().getName().equals("soundproof") && gain_friendship_from_melodies$shouldCheckSoundProof())) {
                MelodyProgress melodyProgress = MelodiesUtil.getMelodyProgress(player);
                long progress = MelodiesUtil.getProgress(melodyProgress);
                long length = MelodiesUtil.getLength(melodyProgress);
                if (progress > 0 && progress < length) {//idk why the progress continues after the song finishes.
                    isHearing = true;
                    int progressToSec = Mth.floor((float) ((progress + 1) / 1000));
                    if (gain_friendship_from_melodies$getMusicState() == -1 || gain_friendship_from_melodies$getMusicState() > progressToSec) {
                        gain_friendship_from_melodies$setMusicState(progressToSec);
                    }
                    int duration = progressToSec - gain_friendship_from_melodies$getMusicState();
                    if (duration % COOLDOWN == 0 && duration > 0) {
                        gain_friendship_from_melodies$setMusicState(progressToSec + 1);
                        getPokemon().incrementFriendship(friendshipValue, true);
                        ClientUtil.makeParticle(5, this, ParticleTypes.HAPPY_VILLAGER);
                        var status = getPokemon().getStatus();
                        if (status != null) {
                            String statusName = status.getStatus().getName().getPath();
                            if (Arrays.stream(GainFriendshipFromMelodies.commonConfig().curable_status).toList().contains(statusName)) {
                                ((PersistentStatusContainerMixin) (Object) status).setSecondsLeft(0);
                            }
                        }
                    }
                    if (length - progress < COOLDOWN * 1000 && GainFriendshipFromMelodies.commonConfig().should_warn && !gain_friendship_from_melodies$hasWarned) {
                        gain_friendship_from_melodies$setMusicState((int) (gain_friendship_from_melodies$getMusicState() + COOLDOWN));
                        cry();
                        gain_friendship_from_melodies$hasWarned = true;
                    }
                }
            }
            if (!isHearing) {
                gain_friendship_from_melodies$setMusicState(-1);
                gain_friendship_from_melodies$hasWarned = false;
            }
        }
    }

    @Unique
    private int gain_friendship_from_melodies$getMusicState() {
        return gain_friendship_from_melodies$musicState;
    }

    @Unique
    private void gain_friendship_from_melodies$setMusicState(int value) {
        gain_friendship_from_melodies$musicState = value;
    }
}
