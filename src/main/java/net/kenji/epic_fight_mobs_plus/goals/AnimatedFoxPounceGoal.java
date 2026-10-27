package net.kenji.epic_fight_mobs_plus.goals;

import net.kenji.epic_fight_mobs_plus.gameasset.animations.MobsPlusAnimations;
import net.kenji.epic_fight_mobs_plus.gameasset.mob_patches.FoxPatch;
import net.kenji.epic_fight_mobs_plus.mixins.accessors.FoxAccessor;
import net.kenji.epic_fight_mobs_plus.network.ClientPlayAnimationPacket;
import net.kenji.epic_fight_mobs_plus.network.MobsPlusPacketHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.JumpGoal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class AnimatedFoxPounceGoal extends JumpGoal {
    final FoxPatch<?> foxPatch;
    final Fox fox;
    public AnimatedFoxPounceGoal(FoxPatch<? extends PathfinderMob> foxPatch) {
        super();
        this.foxPatch = foxPatch;
        this.fox = foxPatch.getOriginal();
        
    }
    public boolean canUse() {
        if (!fox.isFullyCrouched()) {
            return false;
        } else {
            LivingEntity livingentity = fox.getTarget();
            if (livingentity != null && livingentity.isAlive()) {
                if (livingentity.getMotionDirection() != livingentity.getDirection()) {
                    return false;
                } else {
                    boolean flag = Fox.isPathClear(fox, livingentity);
                    if (!flag) {
                        fox.getNavigation().createPath(livingentity, 0);
                        fox.setIsCrouching(false);
                        fox.setIsInterested(false);
                    }

                    return flag;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        LivingEntity livingentity = fox.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            double d0 = fox.getDeltaMovement().y;
            return (!(d0 * d0 < (double)0.05F) || !(Math.abs(fox.getXRot()) < 15.0F) || !fox.onGround()) && !fox.isFaceplanted();
        } else {
            return false;
        }
    }

    public boolean isInterruptable() {
        return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        fox.setJumping(true);
        fox.setIsPouncing(true);
        fox.setIsInterested(false);
        LivingEntity livingentity = fox.getTarget();
        if (livingentity != null) {
            fox.getLookControl().setLookAt(livingentity, 60.0F, 30.0F);
            Vec3 vec3 = (new Vec3(livingentity.getX() - fox.getX(), livingentity.getY() - fox.getY(), livingentity.getZ() - fox.getZ())).normalize();
            fox.setDeltaMovement(fox.getDeltaMovement().add(vec3.x * 0.8D, 0.9D, vec3.z * 0.8D));
        }

        foxPatch.playAnimationSynchronized(MobsPlusAnimations.FOX_POUNCE_LEAP, 0.05F);
        MobsPlusPacketHandler.sendToAll(new ClientPlayAnimationPacket(fox.getId(), MobsPlusAnimations.FOX_POUNCE_LEAP.get().getLocation()));
        fox.getNavigation().stop();
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        fox.setIsCrouching(false);
        ((FoxAccessor)fox).setCrouchAmount(0.0F);
        ((FoxAccessor)fox).setCrouchAmountO(0.0F);
        fox.setIsInterested(false);
        fox.setIsPouncing(false);
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        LivingEntity livingentity = fox.getTarget();
        if (livingentity != null) {
            fox.getLookControl().setLookAt(livingentity, 60.0F, 30.0F);
            if(fox.onGround()){
                foxPatch.playAnimationSynchronized(MobsPlusAnimations.FOX_POUNCE_READY, 0.05F);
                MobsPlusPacketHandler.sendToAll(new ClientPlayAnimationPacket(fox.getId(), MobsPlusAnimations.FOX_POUNCE_READY.get().getLocation()));
            }
        }

        if (!fox.isFaceplanted()) {
            Vec3 vec3 = fox.getDeltaMovement();
            if (vec3.y * vec3.y < (double)0.03F && fox.getXRot() != 0.0F) {
                fox.setXRot(Mth.rotLerp(0.2F, fox.getXRot(), 0.0F));
            } else {
                double d0 = vec3.horizontalDistance();
                double d1 = Math.signum(-vec3.y) * Math.acos(d0 / vec3.length()) * (double)(180F / (float)Math.PI);
                fox.setXRot((float)d1);
            }
        }

        if (livingentity != null && fox.distanceTo(livingentity) <= 2.0F) {
            fox.doHurtTarget(livingentity);
        } else if (fox.getXRot() > 0.0F && fox.onGround() && (float)fox.getDeltaMovement().y != 0.0F && fox.level().getBlockState(fox.blockPosition()).is(Blocks.SNOW)) {
            fox.setXRot(60.0F);
            fox.setTarget((LivingEntity)null);
            ((FoxAccessor)fox).invokeSetFaceplanted(true);
        }

    }
}