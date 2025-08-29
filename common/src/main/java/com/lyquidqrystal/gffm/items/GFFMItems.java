package com.lyquidqrystal.gffm.items;

import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class GFFMItems {
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(GainFriendshipFromMelodies.MOD_ID, Registries.ITEM);

    public static void bootstrap() {
        //GainFriendshipFromMelodies.LOGGER.info("GFFM Registering Items");
        ITEMS.register();
    }
    protected class IMItemData{

    }
}
