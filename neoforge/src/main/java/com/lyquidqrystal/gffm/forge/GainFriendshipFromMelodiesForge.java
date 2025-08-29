package com.lyquidqrystal.gffm.forge;

import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import net.neoforged.fml.common.Mod;


@Mod(GainFriendshipFromMelodies.MOD_ID)
public final class GainFriendshipFromMelodiesForge {
    public GainFriendshipFromMelodiesForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        //EventBuses.registerModEventBus(GainFriendshipFromMelodies.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        GainFriendshipFromMelodies.init();
    }
}
