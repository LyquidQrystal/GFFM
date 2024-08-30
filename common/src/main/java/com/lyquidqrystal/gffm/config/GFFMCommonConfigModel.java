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
    @Comment("The pokemon should be within rhe distance to gain friendship.")
    public int distance_limit = 16;
    @Comment("If your Pokemon will remind you the song is coming to an end by crying.")
    public boolean should_warn = true;
    @Comment("If you can use the instrument in the offhand to increase the friendship")
    public boolean can_use_offhand_item = true;
    @Comment("Status that can be cured by the music(persistent status only)")
    public String[] curable_status={
            "sleep"
    };
    @Comment("Rules to add instrument sound to the Pokemon")
    public String[] distribution_rules={
            "flute|any|any|any|any|any|grasswhistle|100",
            "lute|Kricketune|any|any|any|any|any|100",
            "tiny_drum|Snorlax|any|any|any|any|any|100",
            "tiny_drum|Rillaboom|any|any|any|any|any|100",
            "tiny_drum|any|any|any|any|any|bellydrum|100",
            "triangle|any|any|any|any|any|healbell|100",
            "triangle|Chingling|any|any|any|any|any|100",
            "triangle|Chimecho|any|any|any|any|any|100"
    };
}
