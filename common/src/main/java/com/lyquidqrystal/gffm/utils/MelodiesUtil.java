package com.lyquidqrystal.gffm.utils;

import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.MelodyProgressManager;
import immersive_melodies.item.InstrumentItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MelodiesUtil {
    public static InstrumentItem getInstrumentItem(Player player, boolean getMainHand) {
        ItemStack itemStack = getInstrumentItemStack(player, getMainHand);
        if (itemStack != null && itemStack.getItem() instanceof InstrumentItem ii) {
            return ii;
        }
        return null;
    }

    public static ItemStack getInstrumentItemStack(Player player, boolean getMainHand) {
        if (player != null) {//Game will crash on exit without this line(Fabric only)
            ItemStack itemStack = getMainHand ? player.getMainHandItem() : player.getOffhandItem();

            if (itemStack.getItem() instanceof InstrumentItem) {
                return itemStack;
            }
        }
        return null;
    }

    public static InstrumentItem getInstrumentItem_Final(Player player) {
        ItemStack itemStack = getInstrumentItemStack_Final(player);
        if (itemStack != null && itemStack.getItem() instanceof InstrumentItem ii) {
            return ii;
        }
        return null;
    }

    public static ItemStack getInstrumentItemStack_Final(Player player) {
        boolean shouldCheckOffHand = GainFriendshipFromMelodies.commonConfig().can_use_offhand_item;
        ItemStack itemStack = getInstrumentItemStack(player, true);
        if (shouldCheckOffHand && itemStack == null) {
            itemStack = getInstrumentItemStack(player, false);
        }
        return itemStack;
    }

    public static MelodyProgress getMelodyProgress(Player player) {
        InstrumentItem ii = getInstrumentItem_Final(player);
        if (ii == null) {
            return null;
        }
        return MelodyProgressManager.INSTANCE.getProgress(player);
    }

    public static boolean isPlaying(Player player) {
        ItemStack itemStack = getInstrumentItemStack_Final(player);
        if (itemStack != null && itemStack.getItem() instanceof InstrumentItem ii) {
            return ii.isPlaying(itemStack);//anyone knows why it can't just be a static function???
        }
        return false;
    }

    public static long getProgress(Player player) {
        MelodyProgress melodyProgress = getMelodyProgress(player);
        return getProgress(melodyProgress);
    }

    public static long getLength(Player player) {
        MelodyProgress melodyProgress = getMelodyProgress(player);
        return getLength(melodyProgress);
    }

    public static long getProgress(MelodyProgress melodyProgress) {
        if (melodyProgress == null) {
            return -1;
        }
        return melodyProgress.getTime();
    }

    public static long getLength(MelodyProgress melodyProgress) {
        if (melodyProgress == null) {
            return -1;
        }
        return melodyProgress.getMelody().getLength();
    }
}
