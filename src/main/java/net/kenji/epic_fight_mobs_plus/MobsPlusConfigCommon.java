package net.kenji.epic_fight_mobs_plus;

import net.minecraftforge.common.ForgeConfigSpec;

public class MobsPlusConfigCommon {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<Boolean> REPLACE_EPIC_FIGHT_MOUNT_IDLE;
    public static ForgeConfigSpec.ConfigValue<Boolean> USE_MOUNT_RIDE_ANIMATIONS;

    static {
        BUILDER.push("Chance Values");

        REPLACE_EPIC_FIGHT_MOUNT_IDLE = BUILDER
                .comment("Replace Default Epic Fight Mount Idle")
                .define("Whether The Default Mount Idle Animation Should Be Replaced", true);

        USE_MOUNT_RIDE_ANIMATIONS = BUILDER
                .comment("Use Mount Ride Animations")
                .define("Whether The Player Should Play A Riding Animation When Moving Forward Or Backward On A Mount (Such As A Horse)", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
