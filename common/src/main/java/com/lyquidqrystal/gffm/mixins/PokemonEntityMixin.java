package com.lyquidqrystal.gffm.mixins;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import com.lyquidqrystal.gffm.interfaces.PokemonEntityInterface;
import com.lyquidqrystal.gffm.net.MelodyInfoPacket;
import com.lyquidqrystal.gffm.utils.ClientUtil;
import com.lyquidqrystal.gffm.utils.MelodiesUtil;
import com.lyquidqrystal.gffm.utils.PokemonChecker;
import dev.architectury.networking.NetworkManager;
import immersive_melodies.Common;
import immersive_melodies.Sounds;
import immersive_melodies.client.MelodyProgress;
import immersive_melodies.item.InstrumentItem;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.Note;
import io.netty.buffer.Unpooled;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin extends Mob  implements PokemonEntityInterface {
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
    private int lastNoteIndex;
    private LivingEntity cliendsideCachedOwner;
    private static final EntityDataAccessor<Integer> DATA_ID_OWNER;
    private static final EntityDataAccessor<String> DATA_INSTRUMENT_NAME;
    private static final EntityDataAccessor<Long> MUSIC_PROGRESS;
    private static final EntityDataAccessor<Long> MUSIC_LENGTH;

    static {
        DATA_ID_OWNER = SynchedEntityData.defineId(PokemonEntityMixin.class, EntityDataSerializers.INT);
        DATA_INSTRUMENT_NAME = SynchedEntityData.defineId(PokemonEntityMixin.class, EntityDataSerializers.STRING);
        MUSIC_PROGRESS = SynchedEntityData.defineId(PokemonEntityMixin.class, EntityDataSerializers.LONG);
        MUSIC_LENGTH = SynchedEntityData.defineId(PokemonEntityMixin.class, EntityDataSerializers.LONG);
    }

    protected PokemonEntityMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        gain_friendship_from_melodies$hasWarned = false;
        gain_friendship_from_melodies$musicState = 0;
        lastNoteIndex = 0;
    }

    @Unique
    private boolean gain_friendship_from_melodies$shouldCheckSoundProof() {
        return GainFriendshipFromMelodies.commonConfig().is_soundproof_on;
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void defineExtraSynchedData(CallbackInfo info) {
        entityData.define(DATA_ID_OWNER, -1);//This doesn't need to be saved so there will be no other injections
        entityData.define(DATA_INSTRUMENT_NAME, "");
        entityData.define(MUSIC_PROGRESS, 0L);
        entityData.define(MUSIC_LENGTH,0L);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickInject(CallbackInfo ci) {
        int friendshipValue = GainFriendshipFromMelodies.commonConfig().friendship_increment_value;
        long COOLDOWN = GainFriendshipFromMelodies.commonConfig().increase_friendship_cooldown;
        boolean isHearing = false;

        if (level().isClientSide) {
            //GainFriendshipFromMelodies.LOGGER.info("client side detected");
            if (gain_friendship_from_melodies$getOwnerId() != -1) {
                cliendsideCachedOwner = (LivingEntity) level().getEntity(gain_friendship_from_melodies$getOwnerId());
            } else {
                cliendsideCachedOwner = null;
            }
            if (cliendsideCachedOwner != null) {
                //GainFriendshipFromMelodies.LOGGER.info("Owner Detected");
                gain_friendship_from_melodies$imitate((Player) cliendsideCachedOwner);
            }
        }
        if (this.getPokemon().isPlayerOwned() && !isBattling()) {
            Player player = this.getPokemon().getOwnerPlayer();//The game crashes if the mixin extends the TamableAnimal class, so it might be the easiest way to get the owner.Attention:this is a ServerPlayer.
            gain_friendship_from_melodies$setOwnerId(-1);
            if (getInstrumentName().isEmpty()) {
                for (String rule : GainFriendshipFromMelodies.commonConfig().distribution_rules) {
                    var tmp = PokemonChecker.match(rule, getPokemon());//TODO Rewrite it with entityData
                    if (!Objects.equals(tmp, "")) {
                        setInstrumentName(tmp);
                        break;
                    }
                }
            }
            if (player != null && this.level().dimension() == player.level().dimension() && this.distanceTo(player) < GainFriendshipFromMelodies.commonConfig().distance_limit && !(getPokemon().getAbility().getName().equals("soundproof") && gain_friendship_from_melodies$shouldCheckSoundProof())) {
                long progress;
                long length;
                if (level().isClientSide) {
                    MelodyProgress melodyProgress = MelodiesUtil.getMelodyProgress(player);
                    progress = MelodiesUtil.getProgress(melodyProgress);
                    length = MelodiesUtil.getLength(melodyProgress);
                    MelodyInfoPacket packet=new MelodyInfoPacket(progress,length);
                    NetworkManager.sendToServer(MelodyInfoPacket.MELODY_INFO_PACKET_ID, packet.encode());
                } else {
                    progress = getMusicProgress();
                    length = getMusicLength();
                }
                if (progress > 0 && progress < length) {//idk why the progress continues after the song finishes.
                    isHearing = true;
                    gain_friendship_from_melodies$setOwnerId(player.getId());
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

    @Override
    public long getMusicProgress() {
        return entityData.get(MUSIC_PROGRESS);
    }

    @Override
    public void setMusicProgress(long progress) {
        entityData.set(MUSIC_PROGRESS, progress);
    }

    @Override
    public long getMusicLength() {
        return entityData.get(MUSIC_LENGTH);
    }

    @Override
    public void setMusicLength(long length) {
        entityData.set(MUSIC_LENGTH, length);
    }

    @Unique
    private void gain_friendship_from_melodies$imitate(Player player) {
        InstrumentItem template = MelodiesUtil.getInstrumentItemTemplate(getInstrumentName());//TODO use the config to allow pokemon use different instrument
        if (!level().isClientSide || template == null) {
            return;
        }
        Sounds.Instrument sound = ((InstrumentItemAccessor) template).getSound();
        var InstrumentSustain = ((InstrumentItemAccessor) template).getSustain();
        var offset = ((InstrumentItemAccessor) template).getOffset();
        ItemStack itemStack = MelodiesUtil.getInstrumentItemStack_Final(player);
        InstrumentItem item = MelodiesUtil.getInstrumentItem_Final(player);
        if (item != null) {
            //The following code is a simplified version of InstrumentItem.InventoryClientTick(). Some variables are renamed to distinguish.
            Set<Integer> enabledTracks = item.getEnabledTracks(itemStack);
            MelodyProgress progress = MelodiesUtil.getMelodyProgress(player);
            Melody melody = progress.getMelody();

            for (int track = 0; track < melody.getTracks().size(); track++) {
                int lastIndex = progress.getLastIndex(track);
                List<Note> notes = melody.getTracks().get(track).getNotes();
                if (lastIndex > 0) {
                    lastIndex -= 1;
                }
                if (lastIndex == lastNoteIndex || lastIndex > notes.size() - 1) {
                    return;//The note is played or the melody is changed.
                }
                if (level().isClientSide) {
                    Note note = notes.get(lastIndex);
                    if (progress.getTime() >= note.getTime()) {
                        if (enabledTracks.isEmpty() || enabledTracks.contains(track)) {
                            float volume = note.getVelocity() / 255.0f * 2.0f;
                            float pitch = (float) Math.pow(2, (note.getNote() - 24) / 12.0);
                            int octave = 1;
                            while (octave < 8 && pitch > 4.0 / 3.0) {
                                pitch /= 2;
                                octave++;
                            }
                            long length = note.getLength();
                            long sustain = Math.min(InstrumentSustain, note.getSustain());

                            // sound
                            Common.soundManager.playSound(getX(), getY(), getZ(),
                                    sound.get(octave), SoundSource.NEUTRAL,
                                    volume, pitch, length, sustain,
                                    note.getTime() - progress.getTime(), this);
                            lastNoteIndex = lastIndex;
                            // particle
                            if (!Common.soundManager.isFirstPerson(this)) {
                                double x = Math.sin(-this.yBodyRot / 180.0 * Math.PI);
                                double z = Math.cos(-this.yBodyRot / 180.0 * Math.PI);
                                level().addParticle(ParticleTypes.NOTE,
                                        getX() + x * offset.z + z * offset.x, getY() + getBbHeight() / 2.0 + offset.y, getZ() + z * offset.z - x * offset.x,
                                        x * 5.0, 0.0, z * 5.0);
                            }
                        }
                    }
                }

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

    @Unique
    private Integer gain_friendship_from_melodies$getOwnerId() {
        return entityData.get(DATA_ID_OWNER);
    }

    @Unique
    private void gain_friendship_from_melodies$setOwnerId(Integer id) {
        entityData.set(DATA_ID_OWNER, id);
    }

    @Unique
    private String getInstrumentName() {
        return entityData.get(DATA_INSTRUMENT_NAME);
    }

    @Unique
    private void setInstrumentName(String name) {
        entityData.set(DATA_INSTRUMENT_NAME, name);
    }
}
