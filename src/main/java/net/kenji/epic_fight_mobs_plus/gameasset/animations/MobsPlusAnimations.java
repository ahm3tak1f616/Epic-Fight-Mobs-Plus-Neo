package net.kenji.epic_fight_mobs_plus.gameasset.animations;

import net.kenji.epic_fight_mobs_plus.EpicFightMobsPlus;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleActionAnimation;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleVariantAnimation;
import net.kenji.epic_fight_mobs_plus.api.interfaces.IAnimalMobPatch;
import net.kenji.epic_fight_mobs_plus.api.interfaces.IHorsePatch;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusArmatures;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusColliderPreset;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusLivingMotions;
import net.kenji.epic_fight_mobs_plus.gameasset.armatures.CatArmature;
import net.kenji.epic_fight_mobs_plus.gameasset.armatures.PolarBearArmature;
import net.kenji.epic_fight_mobs_plus.gameasset.armatures.WolfArmature;
import net.kenji.epic_fight_mobs_plus.gameasset.mob_patches.*;
import net.kenji.epic_fight_mobs_plus.mixins.accessors.AbstractHorseAccessor;
import net.kenji.epic_fight_mobs_plus.mixins.accessors.EntityAccessor;
import net.kenji.epic_fight_mobs_plus.mixins.accessors.PathNavigationAccessor;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;

public class MobsPlusAnimations {

    public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(EpicFightMobsPlus.MODID, MobsPlusAnimations::build);    }

    public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_MOUNT_IDLE;
    public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_MOUNT_RIDE_FORWARD;
    public static AnimationManager.AnimationAccessor<StaticAnimation> BIPED_MOUNT_RIDE_BACKWARD;


    public static AnimationManager.AnimationAccessor<IdleVariantAnimation> WOLF_IDLE;
    public static AnimationManager.AnimationAccessor<IdleVariantAnimation> WOLF_IDLE_VAR1;
    public static AnimationManager.AnimationAccessor<IdleVariantAnimation> WOLF_IDLE_VAR2;
    public static AnimationManager.AnimationAccessor<IdleVariantAnimation> WOLF_IDLE_VAR3;

    public static AnimationManager.AnimationAccessor<StaticAnimation> WOLF_WALK;
    public static AnimationManager.AnimationAccessor<StaticAnimation> WOLF_RUN;
    public static AnimationManager.AnimationAccessor<StaticAnimation> WOLF_SITTING;
    public static AnimationManager.AnimationAccessor<IdleActionAnimation> WOLF_SIT_ACTION_1;
    public static AnimationManager.AnimationAccessor<IdleActionAnimation> WOLF_SIT_ACTION_2;
    public static AnimationManager.AnimationAccessor<IdleActionAnimation> WOLF_SIT_ACTION_3;
    public static AnimationManager.AnimationAccessor<IdleActionAnimation> WOLF_SIT_ACTION_4;

    public static AnimationManager.AnimationAccessor<StaticAnimation> WOLF_DEATH;

    public static AnimationManager.AnimationAccessor<IdleActionAnimation> WOLF_IDLE_ACTION_1;
    public static AnimationManager.AnimationAccessor<IdleActionAnimation> WOLF_IDLE_ACTION_2;

    public static AnimationManager.AnimationAccessor<StaticAnimation> WOLF_SHAKE;
    public static AnimationManager.AnimationAccessor<StaticAnimation> WOLF_DYING;

    public static AnimationManager.AnimationAccessor<AttackAnimation> WOLF_ATTACK_1;


    public static AnimationManager.AnimationAccessor<StaticAnimation> HORSE_IDLE;
    public static AnimationManager.AnimationAccessor<StaticAnimation> HORSE_WALK;
    public static AnimationManager.AnimationAccessor<StaticAnimation> HORSE_RUN;
    public static AnimationManager.AnimationAccessor<StaticAnimation> HORSE_JUMP;
    public static AnimationManager.AnimationAccessor<StaticAnimation> HORSE_DEATH;

    public static AnimationManager.AnimationAccessor<IdleActionAnimation> HORSE_IDLE_ACTION_1;
    public static AnimationManager.AnimationAccessor<IdleActionAnimation> HORSE_IDLE_ACTION_2;
    public static AnimationManager.AnimationAccessor<IdleActionAnimation> HORSE_IDLE_ACTION_3;
    public static AnimationManager.AnimationAccessor<IdleActionAnimation> HORSE_IDLE_ACTION_4;


    public static AnimationManager.AnimationAccessor<StaticAnimation> CAT_IDLE;
    public static AnimationManager.AnimationAccessor<IdleVariantAnimation> CAT_IDLE_VAR1;
    public static AnimationManager.AnimationAccessor<IdleVariantAnimation> CAT_IDLE_VAR2;
    public static AnimationManager.AnimationAccessor<IdleVariantAnimation> CAT_IDLE_VAR3;
    public static AnimationManager.AnimationAccessor<StaticAnimation> CAT_WALK;
    public static AnimationManager.AnimationAccessor<StaticAnimation> CAT_RUN;
    public static AnimationManager.AnimationAccessor<StaticAnimation> CAT_SITTING;
    public static AnimationManager.AnimationAccessor<StaticAnimation> CAT_LAYING;

    public static AnimationManager.AnimationAccessor<StaticAnimation> CAT_DEATH;

    public static AnimationManager.AnimationAccessor<AttackAnimation> CAT_ATTACK;

    public static AnimationManager.AnimationAccessor<IdleActionAnimation> CAT_SIT_ACTION_1;
    public static AnimationManager.AnimationAccessor<IdleActionAnimation> CAT_SIT_ACTION_2;
    public static AnimationManager.AnimationAccessor<IdleActionAnimation> CAT_SIT_ACTION_3;

    public static AnimationManager.AnimationAccessor<StaticAnimation> FOX_IDLE;
    public static AnimationManager.AnimationAccessor<StaticAnimation> FOX_WALK;
    public static AnimationManager.AnimationAccessor<StaticAnimation> FOX_RUN;
    public static AnimationManager.AnimationAccessor<StaticAnimation> FOX_SLEEP;
    public static AnimationManager.AnimationAccessor<StaticAnimation> FOX_SIT;

    public static AnimationManager.AnimationAccessor<StaticAnimation> FOX_POUNCE_READY;

    public static AnimationManager.AnimationAccessor<StaticAnimation> FOX_POUNCE_LEAP;


    public static AnimationManager.AnimationAccessor<StaticAnimation> POLAR_BEAR_IDLE;
    public static AnimationManager.AnimationAccessor<StaticAnimation> POLAR_BEAR_WALK;
    public static AnimationManager.AnimationAccessor<StaticAnimation> POLAR_BEAR_RUN;

    public static AnimationManager.AnimationAccessor<AttackAnimation> POLAR_BEAR_ATTACK;

    private static void build(AnimationManager.AnimationBuilder builder){
        BIPED_MOUNT_IDLE = builder.nextAccessor("biped/living/mount_idle", (accessor -> new StaticAnimation(0.2F,true, accessor, Armatures.BIPED)));

        BIPED_MOUNT_RIDE_FORWARD = builder.nextAccessor("biped/living/mount_ride_forward", (accessor -> new StaticAnimation(0.2F,true, accessor, Armatures.BIPED)));
        BIPED_MOUNT_RIDE_BACKWARD = builder.nextAccessor("biped/living/mount_ride_backward", (accessor -> new StaticAnimation(0.2F,true, accessor, Armatures.BIPED)));


        WOLF_IDLE = builder.nextAccessor("wolf/living/wolf_idle", (accessor -> new IdleVariantAnimation(8, 2, LivingMotions.IDLE, 0.2F, true, accessor, MobsPlusArmatures.WOLF)));
        WOLF_IDLE_VAR1 = builder.nextAccessor("wolf/living/wolf_idle_var1", (accessor -> new IdleVariantAnimation(5, 3, MobsPlusLivingMotions.IDLE_VAR2, 0.2F, true, accessor, MobsPlusArmatures.WOLF)));
        WOLF_IDLE_VAR2 = builder.nextAccessor("wolf/living/wolf_idle_var2", (accessor -> new IdleVariantAnimation(2, 4,MobsPlusLivingMotions.IDLE_VAR3, 0.2F, true, accessor, MobsPlusArmatures.WOLF)));
        WOLF_IDLE_VAR3 = builder.nextAccessor("wolf/living/wolf_idle_var3", (accessor -> new IdleVariantAnimation(3, 2.5,MobsPlusLivingMotions.IDLE_VAR4, 0.2F, true, accessor, MobsPlusArmatures.WOLF)));

        WOLF_WALK = builder.nextAccessor("wolf/living/wolf_walk", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.WOLF)));
        WOLF_RUN = builder.nextAccessor("wolf/living/wolf_run", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.WOLF).addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
            if (entitypatch instanceof IAnimalMobPatch patchInterface) {
                return patchInterface.getAnimForwardSpeed(0.8F, 1.9F);
            }
            return speed;
        })));
        WOLF_SITTING = builder.nextAccessor("wolf/living/wolf_sit", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.WOLF)));
        WOLF_DEATH = builder.nextAccessor("wolf/living/wolf_death", (accessor -> new StaticAnimation(0.2F, false, accessor, MobsPlusArmatures.WOLF)));

        WOLF_IDLE_ACTION_1 = builder.nextAccessor("wolf/living/wolf_idle_action_1", (accessor -> new IdleActionAnimation(8, 4.5,0, 2, 16,0.1F, false, accessor, MobsPlusArmatures.WOLF)));
        WOLF_IDLE_ACTION_2 = builder.nextAccessor("wolf/living/wolf_idle_action_2", (accessor -> new IdleActionAnimation(5, 8,0, 3.5, 18, 0.1F, false, accessor, MobsPlusArmatures.WOLF)));
        WOLF_SIT_ACTION_1 = builder.nextAccessor("wolf/living/wolf_sit_action_1", (accessor -> new IdleActionAnimation(5, 8,0, 3.5, 18, 0.1F, false, accessor, MobsPlusArmatures.WOLF)));
        WOLF_SIT_ACTION_2 = builder.nextAccessor("wolf/living/wolf_sit_action_2", (accessor -> new IdleActionAnimation(3, 10,0, 3.5, 18, 0.1F, false, accessor, MobsPlusArmatures.WOLF)));
        WOLF_SIT_ACTION_3 = builder.nextAccessor("wolf/living/wolf_sit_action_3", (accessor -> new IdleActionAnimation(4, 9,0, 3.5, 18, 0.1F, false, accessor, MobsPlusArmatures.WOLF)));
        WOLF_SIT_ACTION_4 = builder.nextAccessor("wolf/living/wolf_sit_action_4", (accessor -> new IdleActionAnimation(2, 12,0, 3.5, 18, 0.1F, false, accessor, MobsPlusArmatures.WOLF)));

        WOLF_SHAKE = builder.nextAccessor("wolf/living/wolf_shake", (accessor -> new StaticAnimation(0.1F, false, accessor, MobsPlusArmatures.WOLF)));
        WOLF_DYING = builder.nextAccessor("wolf/living/wolf_dying", (accessor -> new StaticAnimation(0.1F, true, accessor, MobsPlusArmatures.WOLF)));

        WOLF_ATTACK_1 = builder.nextAccessor("wolf/combat/wolf_attack_1", (accessor) -> new AttackAnimation(0.15F, 0.22F, 0.30F, 0.38F, 0.44F, ColliderPreset.FIST, ((WolfArmature) MobsPlusArmatures.WOLF.get()).head, accessor, MobsPlusArmatures.WOLF));


        HORSE_IDLE = builder.nextAccessor("horse/living/horse_idle", (accessor -> new StaticAnimation(0.2F, true, accessor, MobsPlusArmatures.HORSE)));
        HORSE_IDLE_ACTION_1 = builder.nextAccessor("horse/living/horse_idle_action_1", (accessor -> new IdleActionAnimation(15, 6,0, 2, 18.5,0.2F, false, accessor, MobsPlusArmatures.HORSE)));
        HORSE_IDLE_ACTION_2 = builder.nextAccessor("horse/living/horse_idle_action_2", (accessor -> new IdleActionAnimation(8, 7.5,0, 2, 18.5,0.2F, false, accessor, MobsPlusArmatures.HORSE)));
        HORSE_IDLE_ACTION_3 = builder.nextAccessor("horse/living/horse_idle_action_3", (accessor -> new IdleActionAnimation(20, 6,0, 3, 18.5, 0.1F, false, accessor, MobsPlusArmatures.HORSE)));
        HORSE_IDLE_ACTION_4 = builder.nextAccessor("horse/living/horse_idle_action_4", (accessor -> new IdleActionAnimation(26, 2.5,0, 1.85F, 16.5, 0.1F, false, accessor, MobsPlusArmatures.HORSE)));


        HORSE_WALK = builder.nextAccessor("horse/living/horse_walk", (accessor -> new StaticAnimation(0.25F, true, accessor, MobsPlusArmatures.HORSE)
                .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
                    if (entitypatch instanceof IAnimalMobPatch patchInterface) {
                        return patchInterface.getAnimForwardSpeed(0.28F, 1.85F);
                    }
                    return speed;
                })));
        HORSE_RUN = builder.nextAccessor("horse/living/horse_run", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.HORSE)));
        HORSE_JUMP = builder.nextAccessor("horse/living/horse_jump", (accessor -> new StaticAnimation(0.05F, false, accessor, MobsPlusArmatures.HORSE).addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
            if (entitypatch instanceof IAnimalMobPatch patchInterface) {
                if (!(patchInterface.getEntityPatch().getOriginal() instanceof AbstractHorse horse)) return speed;
                if (!(patchInterface.getEntityPatch() instanceof IHorsePatch horsePatch)) return speed;

                if (horsePatch.getStoredJumpSpeed() != -1) return horsePatch.getStoredJumpSpeed();

                // 1.21.1: getCustomJump() is gone, use the JUMP_STRENGTH attribute instead
                double jumpStrength = horse.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH);
                double d0 = jumpStrength
                        * (double)((AbstractHorseAccessor) horse).getPlayerJumpScale()
                        * (double)((EntityAccessor) horse).invokeGetBlockJumpFactor();
                float boost = (float)(d0 + (double) horse.getJumpBoostPower());

                // normalize boost into 0 → 1 range (roughly)
                float normalized = Math.min(boost, 1.0F);

                // invert it
                float multiplier = 1.0F + (1.0F - normalized);
                float finalSpeed = speed * multiplier;

                if (finalSpeed <= 1.8F) finalSpeed = 1.0F;
                if (finalSpeed >= 2.0F) finalSpeed = 2.25F;

                return horsePatch.setStoredJumpSpeed(finalSpeed);
            }
            return speed;
        })));
        HORSE_DEATH = builder.nextAccessor("horse/living/horse_death", (accessor -> new StaticAnimation(0.2F, false, accessor, MobsPlusArmatures.HORSE)));

        CAT_IDLE = builder.nextAccessor("cat/living/cat_idle", (accessor -> new StaticAnimation(0.2F, true, accessor, MobsPlusArmatures.CAT)));
        CAT_IDLE_VAR1 = builder.nextAccessor("cat/living/cat_idle_var1", (accessor -> new IdleVariantAnimation(5, 3, MobsPlusLivingMotions.IDLE_VAR2, 0.2F, true, accessor, MobsPlusArmatures.CAT)));
        CAT_IDLE_VAR2 = builder.nextAccessor("cat/living/car_idle_var2", (accessor -> new IdleVariantAnimation(2, 4,MobsPlusLivingMotions.IDLE_VAR3, 0.2F, true, accessor, MobsPlusArmatures.CAT)));
        CAT_IDLE_VAR3 = builder.nextAccessor("cat/living/cat_idle_var3", (accessor -> new IdleVariantAnimation(3, 2.5,MobsPlusLivingMotions.IDLE_VAR4, 0.2F, true, accessor, MobsPlusArmatures.CAT)));

        CAT_WALK = builder.nextAccessor("cat/living/cat_walk", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.CAT)));
        CAT_RUN = builder.nextAccessor("cat/living/cat_run", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.CAT).addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
            if(entitypatch instanceof CatPatch<?> catPatch){
                return (float) (((PathNavigationAccessor)catPatch.getOriginal().getNavigation()).getSpeedModifier() * 2) + speed;
            }
            return speed;
        })));
        CAT_SITTING = builder.nextAccessor("cat/living/cat_sit", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.CAT)));
        CAT_LAYING = builder.nextAccessor("cat/living/cat_lay", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.CAT)));

        CAT_DEATH = builder.nextAccessor("cat/living/cat_death", (accessor -> new StaticAnimation(0.2F, false, accessor, MobsPlusArmatures.CAT)));

        CAT_ATTACK = builder.nextAccessor("cat/combat/cat_attack", (accessor) -> new AttackAnimation(0.15F, 0.08F, 0.10F, 0.12F, 0.5F, ColliderPreset.FIST, ((CatArmature) MobsPlusArmatures.CAT.get()).head, accessor, MobsPlusArmatures.CAT));

        CAT_SIT_ACTION_1 = builder.nextAccessor("cat/living/cat_sit_action_1", (accessor -> new IdleActionAnimation(5, 8,0, 3.5, 18, false, 0.1F, false, accessor, MobsPlusArmatures.CAT)));
        CAT_SIT_ACTION_2 = builder.nextAccessor("cat/living/cat_sit_action_2", (accessor -> new IdleActionAnimation(3, 10,0, 3.5, 18, false,0.1F,false, accessor, MobsPlusArmatures.CAT)));
        CAT_SIT_ACTION_3 = builder.nextAccessor("cat/living/cat_sit_action_3", (accessor -> new IdleActionAnimation(4, 9,0, 3.5, 18, false,0.1F,false, accessor, MobsPlusArmatures.CAT)));

        FOX_IDLE = builder.nextAccessor("fox/living/fox_idle", (accessor -> new StaticAnimation(0.2F, true, accessor, MobsPlusArmatures.FOX)));
        FOX_WALK = builder.nextAccessor("fox/living/fox_walk", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.FOX)));
        FOX_RUN = builder.nextAccessor("fox/living/fox_run", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.FOX)  .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
            if (entitypatch instanceof IAnimalMobPatch patchInterface) {
                if(patchInterface.getEntityPatch().getOriginal() instanceof Fox fox)
                    return (float) (((PathNavigationAccessor)fox.getNavigation()).getSpeedModifier() * 2) + speed;
            }
            return speed;
        })));
        FOX_SLEEP = builder.nextAccessor("fox/living/fox_sleep", (accessor -> new StaticAnimation(0.2F, true, accessor, MobsPlusArmatures.FOX)));
        FOX_SIT = builder.nextAccessor("fox/living/fox_sit", (accessor -> new StaticAnimation(0.2F, true, accessor, MobsPlusArmatures.FOX)));
        FOX_POUNCE_READY = builder.nextAccessor("fox/living/fox_pounce_ready", (accessor -> new StaticAnimation(0.2F, true, accessor, MobsPlusArmatures.FOX)));
        FOX_POUNCE_LEAP = builder.nextAccessor("fox/living/fox_pounce_leap", (accessor -> new StaticAnimation(0.2F, false, accessor, MobsPlusArmatures.FOX).addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
            return 1.6F;
        })));
        POLAR_BEAR_IDLE = builder.nextAccessor("polar_bear/living/polar_bear_idle", (accessor -> new StaticAnimation(0.2F, true, accessor, MobsPlusArmatures.POLAR_BEAR)));
        POLAR_BEAR_WALK = builder.nextAccessor("polar_bear/living/polar_bear_walk", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.POLAR_BEAR)));
        POLAR_BEAR_RUN = builder.nextAccessor("polar_bear/living/polar_bear_run", (accessor -> new StaticAnimation(0.2F,true, accessor, MobsPlusArmatures.POLAR_BEAR).addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
            if(entitypatch instanceof PolarBearPatch<?> catPatch){
                return (float) (((PathNavigationAccessor)catPatch.getOriginal().getNavigation()).getSpeedModifier() * 2) + speed;
            }
            return speed;
        })));
        POLAR_BEAR_ATTACK = builder.nextAccessor("polar_bear/combat/polar_bear_attack", (accessor) -> new AttackAnimation(0.15F, 0.10F, 0.15F, 0.24F, 0.5F, MobsPlusColliderPreset.POLAR_BEAR_COLLIDER, ((PolarBearArmature) MobsPlusArmatures.POLAR_BEAR.get()).chestUpper, accessor, MobsPlusArmatures.POLAR_BEAR));

    }


}

