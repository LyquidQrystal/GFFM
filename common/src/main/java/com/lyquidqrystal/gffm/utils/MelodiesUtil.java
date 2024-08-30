package com.lyquidqrystal.gffm.utils;

import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import immersive_melodies.Items;
import immersive_melodies.client.MelodyProgress;
import immersive_melodies.client.MelodyProgressManager;
import immersive_melodies.item.InstrumentItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MelodiesUtil {
    public static final String MOD_ID = "immersive_melodies";

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
    /*
        Supplier<Item> BAGPIPE = register(Common.MOD_ID, "bagpipe", 200, new Vector3f(0.5f, 0.6f, 0.05f));
    Supplier<Item> DIDGERIDOO = register(Common.MOD_ID, "didgeridoo", 200, new Vector3f(0.0f, -0.45f, 1.0f));
    Supplier<Item> FLUTE = register(Common.MOD_ID, "flute", 100, new Vector3f(0.0f, 0.15f, 0.9f));
    Supplier<Item> LUTE = register(Common.MOD_ID, "lute", 200, new Vector3f(0.0f, 0.0f, 0.5f));
    Supplier<Item> PIANO = register(Common.MOD_ID, "piano", 300, new Vector3f(0.0f, 0.25f, 0.5f));
    Supplier<Item> TRIANGLE = register(Common.MOD_ID, "triangle", 300, new Vector3f(0.0f, 0.0f, 0.6f));
    Supplier<Item> TRUMPET = register(Common.MOD_ID, "trumpet", 100, new Vector3f(0.0f, 0.25f, 1.4f));
    Supplier<Item> TINY_DRUM = register(Common.MOD_ID, "tiny_drum", 300, new Vector3f(0.0f, 0.25f, 0.5f));
     */
    public static InstrumentItem getInstrumentItemTemplate(String name){
        Item item;
        switch (name){
            case "bagpipe":item=Items.BAGPIPE.get();
            case "didgeridoo":item=Items.DIDGERIDOO.get();
            case "flute": item=Items.FLUTE.get();
            case "lute":item=Items.LUTE.get();
            case "piano":item=Items.PIANO.get();
            case "triangle":item=Items.TRIANGLE.get();
            case "trumpet":item=Items.TRUMPET.get();
            case "tiny_drum":item=Items.TINY_DRUM.get();
            default:item=null;
        }
        return (InstrumentItem) item;
    }
}
