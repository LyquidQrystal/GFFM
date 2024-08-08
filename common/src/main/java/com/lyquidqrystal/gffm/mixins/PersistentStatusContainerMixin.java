package com.lyquidqrystal.gffm.mixins;

import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PersistentStatusContainer.class)
public interface PersistentStatusContainerMixin{
    @Accessor("secondsLeft")
    void setSecondsLeft(int secondsLeft);
}
