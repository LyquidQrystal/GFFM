package com.lyquidqrystal.gffm.config;

import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = GainFriendshipFromMelodies.MOD_ID)
public class GFFMCommonConfigModel implements ConfigData {
    @Comment("If the soundproof Pokemon is immune to the music")
    public boolean is_soundproof_on = true;
    @Comment("The friendship value the Pokemon can get when the cooldown is ready.")
    public int friendship_increment_value = 2;
    @Comment("The time needed to get friendship")
    public int increase_friendship_cooldown = 10;
    @Comment("If your Pokemon will remind you the song is coming to an end by crying.")
    public boolean should_warn = true;
    @Comment("If you can use the instrument in the offhand to increase the friendship")
    public boolean can_use_offhand_item = true;
}
