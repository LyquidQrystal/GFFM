package com.lyquidqrystal.gffm.mixins;

import immersive_melodies.Sounds;
import immersive_melodies.item.InstrumentItem;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InstrumentItem.class)
public interface InstrumentItemAccessor {
    @Accessor("sound")
    Sounds.Instrument getSound();

    @Accessor("sustain")
    long getSustain();

    @Accessor("offset")
    Vector3f getOffset();
}
