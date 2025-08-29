package com.lyquidqrystal.gffm;

import com.lyquidqrystal.gffm.config.GFFMCommonConfigModel;
import com.lyquidqrystal.gffm.items.GFFMItems;
import com.lyquidqrystal.gffm.net.GFFMNetwork;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GainFriendshipFromMelodies {
    public static final String MOD_ID = "gain_friendship_from_melodies";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static GFFMCommonConfigModel COMMON_CONFIG;

    public static void init() {
        // Write common init code here.
        AutoConfig.register(GFFMCommonConfigModel.class, JanksonConfigSerializer::new);
        COMMON_CONFIG = AutoConfig.getConfigHolder(GFFMCommonConfigModel.class).getConfig();
        LOGGER.info("GFFM Initiating.");
        GFFMNetwork.init();
        GFFMItems.bootstrap();
    }

    public static GFFMCommonConfigModel commonConfig() {
        return COMMON_CONFIG;
    }


}
