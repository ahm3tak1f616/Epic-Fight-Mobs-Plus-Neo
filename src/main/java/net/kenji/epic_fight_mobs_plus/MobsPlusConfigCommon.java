package net.kenji.epic_fight_mobs_plus;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MobsPlusConfigCommon {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.BooleanValue REPLACE_EPIC_FIGHT_MOUNT_IDLE;
    public static ModConfigSpec.BooleanValue USE_MOUNT_RIDE_ANIMATIONS;

    static {
        BUILDER.push("Mount Animations");

        REPLACE_EPIC_FIGHT_MOUNT_IDLE = BUILDER
                .comment("Whether The Default Mount Idle Animation Should Be Replaced")
                .define("replace_epic_fight_mount_idle", true);

        USE_MOUNT_RIDE_ANIMATIONS = BUILDER
                .comment("Whether The Player Should Play A Riding Animation When Moving Forward Or Backward On A Mount (Such As A Horse)")
                .define("use_mount_ride_animations", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}