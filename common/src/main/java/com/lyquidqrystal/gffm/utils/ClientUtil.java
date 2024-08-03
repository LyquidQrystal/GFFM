package com.lyquidqrystal.gffm.utils;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientUtil {
    public static void makeParticle(int particleAmount, Entity entity, SimpleParticleType particleType) {
        Level level = entity.level();
        if (particleAmount > 0) {
            double d = 0;
            double e = 0;
            double f = 0;
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(particleType, entity.getRandomX(0.5), entity.getRandomY(), entity.getRandomZ(0.5), particleAmount, d, e, f, 1f);
            } else {
                for (int j = 0; j < particleAmount; ++j) {
                    level.addParticle(particleType, entity.getRandomX(0.5), entity.getRandomY(), entity.getRandomZ(0.5), d, e, f);
                }
            }
        }
    }
}
