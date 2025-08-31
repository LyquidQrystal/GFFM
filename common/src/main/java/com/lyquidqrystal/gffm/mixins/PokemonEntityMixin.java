package com.lyquidqrystal.gffm.mixins;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import com.lyquidqrystal.gffm.interfaces.PokemonEntityInterface;
import com.lyquidqrystal.gffm.net.packet.MelodyInfoPacket;
import com.lyquidqrystal.gffm.utils.ClientUtil;
import com.lyquidqrystal.gffm.utils.MelodiesUtil;
import com.lyquidqrystal.gffm.utils.PokemonChecker;
import dev.architectury.networking.NetworkManager;
import immersive_melodies.Common;
import immersive_melodies.Config;
import immersive_melodies.Sounds;
import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.sound.CancelableSoundInstance;
import immersive_melodies.item.InstrumentItem;
import immersive_melodies.resources.Melody;
import immersive_melodies.resources.Note;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin extends Mob implements PokemonEntityInterface {
    @Shadow
    public abstract Pokemon getPokemon();

    @Shadow
    public abstract void cry();

    @Shadow
    public abstract void playAmbientSound();

    @Shadow
    public abstract boolean isBattling();

    @Unique
    private int musicState;
    @Unique
    private boolean hasWarned;
    private HashMap<Integer, Integer> pkmProgress = new HashMap<>();
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
        hasWarned = false;
        musicState = 0;
    }

    @Unique
    private boolean shouldCheckSoundProof() {
        return GainFriendshipFromMelodies.commonConfig().is_soundproof_on;
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void defineExtraSynchedData(SynchedEntityData.Builder builder, CallbackInfo info) {
        builder.define(DATA_ID_OWNER, -1);//This doesn't need to be saved so there will be no other injections
        builder.define(DATA_INSTRUMENT_NAME, "");
        builder.define(MUSIC_PROGRESS, 0L);
        builder.define(MUSIC_LENGTH, 0L);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickInject(CallbackInfo ci) {
        int friendshipValue = GainFriendshipFromMelodies.commonConfig().friendship_increment_value;
        long COOLDOWN = GainFriendshipFromMelodies.commonConfig().increase_friendship_cooldown;
        boolean isHearing = false;
        if (level().isClientSide) {
            if (getOwnerId() != -1) {
                cliendsideCachedOwner = (LivingEntity) level().getEntity(getOwnerId());
            } else {
                cliendsideCachedOwner = null;
            }
            if (cliendsideCachedOwner != null) {
                Player p = (Player) cliendsideCachedOwner;
                MelodyProgress melodyProgress = MelodiesUtil.getMelodyProgress(p);
                if (melodyProgress == null) {
                    return;
                }
                long progressAccurate = MelodiesUtil.getProgress(melodyProgress);
                long lengthAccurate = MelodiesUtil.getLength(melodyProgress);
                long progressProcessed = Mth.floor((float) progressAccurate / 200) * 200L;
                long currentProgress = getMusicProgress();
                Melody melody = melodyProgress.getMelody();
                if (getMusicLength() != melody.getLength() || getMusicLength() == 0 || currentProgress - progressProcessed > 1000) {
                    refresh(melody);
                }
                if (progressProcessed > currentProgress) {
                    MelodyInfoPacket packet = new MelodyInfoPacket(progressProcessed, lengthAccurate);
                    NetworkManager.sendToServer(packet);
                } else {
                    if (currentProgress - progressProcessed > 1000) {
                        MelodyInfoPacket packet = new MelodyInfoPacket(progressProcessed, lengthAccurate);
                        NetworkManager.sendToServer(packet);
                    }
                }
                imitate(p);
            }
        }
        if (this.getPokemon().isPlayerOwned() && !isBattling()) {
            Player player = this.getPokemon().getOwnerPlayer();//The game crashes if the mixin extends the TamableAnimal class, so it might be the easiest way to get the owner.Attention:this is a ServerPlayer.
            if (player != null) {
                if (getOwnerId() == -1) {
                    setOwnerId(player.getId());
                }
                initInstrument();
                if (this.level().dimension() == player.level().dimension() && this.distanceTo(player) < GainFriendshipFromMelodies.commonConfig().distance_limit && !(getPokemon().getAbility().getName().equals("soundproof") && shouldCheckSoundProof())) {
                    long progress = getMusicProgress();
                    long length = getMusicLength();
                    if (progress > 0 && progress < length) {//idk why the progress continues after the song finishes.
                        isHearing = true;
                        int progressToSec = Mth.floor((float) ((progress + 1) / 1000));
                        if (getMusicState() == -1 || getMusicState() > progressToSec) {
                            setMusicState(progressToSec);
                        }
                        int duration = progressToSec - getMusicState();
                        if (duration % COOLDOWN == 0 && duration > 0) {
                            setMusicState(progressToSec + 1);
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
                        if (length - progress < COOLDOWN * 1000 && GainFriendshipFromMelodies.commonConfig().should_warn && !hasWarned) {
                            setMusicState((int) (getMusicState() + COOLDOWN));
                            cry();
                            hasWarned = true;
                        }
                    }
                }
                if (!isHearing) {
                    setMusicState(-1);
                    hasWarned = false;
                }
            }
        }
    }

    @Override
    public long getMusicProgress() {
        return entityData.get(MUSIC_PROGRESS);
    }

    @Override
    public void setMusicProgress(long progress) {
        //GainFriendshipFromMelodies.LOGGER.info("SIDE TEST");
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

    private void refresh(Melody melody) {
        int trackCount = melody.getTracks().size();
        pkmProgress.clear();
        for (int i = 0; i < trackCount; ++i) {
            pkmProgress.put(i, 0);
        }
    }

    @Unique
    private void imitate(Player player) {
        InstrumentItem template = MelodiesUtil.getInstrumentItemTemplate(getInstrumentName());
        Pokemon pokemon = getPokemon();
        var status = pokemon.getStatus();
        if ((status != null && status.getStatus().getName().getPath().equals("sleep")) || !level().isClientSide || template == null) {
            return;
        }
        var acc = (InstrumentItemAccessor) template;
        Sounds.Instrument sound = acc.getSound();
        var instrumentSustain = acc.getSustain();
        var offset = acc.getOffset();
        ItemStack itemStack = MelodiesUtil.getInstrumentItemStack_Final(player);
        InstrumentItem item = MelodiesUtil.getInstrumentItem_Final(player);
        if (item != null) {
            //The following code is a simplified version of InstrumentItem.InventoryClientTick(). Some variables are renamed to distinguish.
            List<Integer> enabledTracks = item.getEnabledTracks(itemStack);
            MelodyProgress progress = MelodiesUtil.getMelodyProgress(player);
            if (progress == null) {
                return;
            }
            Melody melody = progress.getMelody();
            for (int track = 0; track < melody.getTracks().size(); ++track) {
                int lastIndex = pkmProgress.getOrDefault(track, -1);
                if (lastIndex == -1) {
                    continue;
                }
                List<Note> notes = melody.getTracks().get(track).getNotes();
                if (level().isClientSide) {
                    for (int i = lastIndex; i < notes.size(); ++i) {
                        Note note = notes.get(i);
                        if (progress.getTime() < (long) note.getTime()) {
                            pkmProgress.replace(track, i);
                            break;
                        }
                        if (enabledTracks.isEmpty() || enabledTracks.contains(track)) {
                            playNote(note, progress.getTime(), instrumentSustain, offset, sound);
                        }
                        if (i == notes.size() - 1) {
                            pkmProgress.replace(track, i + 1);
                        }
                    }
                }
            }
        }
    }

    protected void playNote(Note note, long time, long insSustain, Vector3f offset, Sounds.Instrument sound) {
        float volume = (float) note.getVelocity() / 255.0F * 2.0F * Config.getInstance().instrumentVolumeFactor;
        float pitch = (float) Math.pow(2.0, (double) (note.getNote() - 24) / 12.0);

        int octave;
        for (octave = 1; octave < 8 && (double) pitch > 1.3333333333333333; ++octave) {
            pitch /= 2.0F;
        }

        long length = note.getLength();
        long sustain = Math.min(insSustain, note.getSustain());
        float factor = Config.getInstance().perceivedLoudnessAdjustmentFactor;
        float adjustedVolume = (float) ((double) volume / Math.sqrt((double) pitch * Math.pow(2.0, (octave - 4))));
        volume = volume * (1.0F - factor) + adjustedVolume * factor;
        Common.soundManager.playSound(getX(), getY(), getZ(), sound.get(octave), SoundSource.NEUTRAL, volume, pitch, length, sustain, (long) note.getTime() - time, this);
        if (Config.getInstance().stopGameMusicForMobs) {
            Common.soundManager.pauseGameMusic();
        }
        if (!Common.soundManager.isFirstPerson(this)) {
            double x = Math.sin((double) (-this.yBodyRot) / 180.0 * Math.PI);
            double z = Math.cos((double) (-this.yBodyRot) / 180.0 * Math.PI);
            this.level().addParticle(ParticleTypes.NOTE, this.getX() + x * (double) offset.z + z * (double) offset.x, this.getY() + (double) this.getBbHeight() / 2.0 + (double) offset.y, this.getZ() + z * (double) offset.z - x * (double) offset.x, x * 5.0, 0.0, z * 5.0);
        }
    }

    protected void initInstrument() {
        if (tickCount % 5 == 0) {
            tryToPickInstrument();
            Pokemon p = getPokemon();
            if (p.getFriendship() >= GainFriendshipFromMelodies.commonConfig().required_friendship_to_play) {
                if (p.heldItem().getItem() instanceof InstrumentItem item) {
                    var rl = BuiltInRegistries.ITEM.getKey(item);
                    setInstrumentName(rl.getPath());
                    return;
                }
            }
            for (String rule : GainFriendshipFromMelodies.commonConfig().distribution_rules) {
                var tmp = PokemonChecker.match(rule, getPokemon());//TODO Rewrite it with entityData
                if (!Objects.equals(tmp, "")) {
                    setInstrumentName(tmp);
                    break;
                }
            }
        }
    }

    protected void tryToPickInstrument() {
        if (GainFriendshipFromMelodies.commonConfig().can_pick_instrument) {
            Pokemon pokemon = getPokemon();
            if (pokemon.heldItem().isEmpty() && pokemon.isPlayerOwned()) {
                Vec3i pickUpReach = getPickupReach();
                var itemList = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(pickUpReach.getX(), pickUpReach.getY(), pickUpReach.getZ()));
                var it = itemList.iterator();
                while (true) {
                    ItemEntity itementity;
                    do {
                        if (!it.hasNext()) {
                            return;
                        }
                        itementity = it.next();
                    } while (itementity.getOwner() != null && itementity.getOwner().getUUID().equals(this.getUUID()));
                    if (!itementity.isRemoved() && !itementity.getItem().isEmpty() && itementity.getItem().getItem() instanceof InstrumentItem) {
                        pickInstrument(itementity);
                    }
                }
            }
        }
    }

    protected void pickInstrument(ItemEntity itemEntity) {
        Pokemon pokemon = getPokemon();
        ItemStack itemStack = itemEntity.getItem();
        pokemon.swapHeldItem(itemStack.copy(), false);
        onItemPickup(itemEntity);
        take(itemEntity, itemStack.getCount());
        itemEntity.discard();
    }


    @Unique
    private int getMusicState() {
        return musicState;
    }

    @Unique
    private void setMusicState(int value) {
        musicState = value;
    }

    @Unique
    private Integer getOwnerId() {
        return entityData.get(DATA_ID_OWNER);
    }

    @Unique
    private void setOwnerId(Integer id) {
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
