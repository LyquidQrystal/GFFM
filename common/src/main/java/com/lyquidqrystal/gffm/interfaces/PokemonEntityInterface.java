package com.lyquidqrystal.gffm.interfaces;

import org.spongepowered.asm.mixin.Unique;

public interface PokemonEntityInterface {
    long getMusicProgress();


    void setMusicProgress(long progress);


    long getMusicLength();


    void setMusicLength(long length);


}
