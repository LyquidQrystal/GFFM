package com.lyquidqrystal.gffm.utils;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientUtil {
    public static void makeParticle(int particleAmount, Entity entity, SimpleParticleType particleType) {
        Level level = entity.level();
        if (particleAmount > 0) {
            double d = entity.getBbWidth() / 2;
            double e = entity.getBbHeight() / 2;
            double f = entity.getBbWidth() / 2;
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(particleType, entity.getX(), entity.getY(), entity.getZ(), particleAmount, d, e, f, 1f);
            } else {
                for (int j = 0; j < particleAmount; ++j) {
                    level.addParticle(particleType, entity.getX(), entity.getY(), entity.getZ(), d, e, f);
                }
            }
        }
    }
}
