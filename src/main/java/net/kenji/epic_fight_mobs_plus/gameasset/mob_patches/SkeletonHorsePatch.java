package net.kenji.epic_fight_mobs_plus.gameasset.mob_patches;

import com.mojang.datafixers.util.Pair;
import net.kenji.epic_fight_mobs_plus.api.abstract_classes.AnimalPatchBase;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleActionAnimation;
import net.kenji.epic_fight_mobs_plus.api.interfaces.IHorsePatch;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusLivingMotions;
import net.kenji.epic_fight_mobs_plus.gameasset.animations.MobsPlusAnimations;
import net.kenji.epic_fight_mobs_plus.mixins.accessors.LivingEntityAccessor;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.List;

public class SkeletonHorsePatch<H extends AbstractHorse> extends AnimalPatchBase<SkeletonHorse> implements IHorsePatch {


    public SkeletonHorsePatch(SkeletonHorse entity) {
        super(entity);
    }

    @Override
    public void updateMotion(boolean b) {
        super.commonMobUpdateMotion(b);
    }


    public float storedJumpSpeed = -1;
    public boolean cachedShouldRun = false;

    @Override
    public void postTick() {
        super.postTick();
        if(getStoredJumpSpeed() != -1 && this.getOriginal().onGround()){
            setStoredJumpSpeed(-1);
        }
    }

    @Override
    public AssetAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        return null;
    }


    public boolean computeShouldRun() {
        boolean followGoalActive = false;

        return false;
    }
    @Override
    public float setStoredJumpSpeed(float value){
        return storedJumpSpeed = value;
    }
    @Override
    public float getStoredJumpSpeed(){
        return storedJumpSpeed;
    }


    @Override
    protected void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, MobsPlusAnimations.HORSE_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, MobsPlusAnimations.HORSE_WALK);
        animator.addLivingAnimation(LivingMotions.CHASE, MobsPlusAnimations.HORSE_RUN);
        animator.addLivingAnimation(LivingMotions.JUMP, MobsPlusAnimations.HORSE_JUMP);
        animator.addLivingAnimation(LivingMotions.DEATH, MobsPlusAnimations.HORSE_DEATH);

    }

    @Override
    public LivingMotion getOptionalLivingMotion() {
        return this.currentOptionalLivingMotion;
    }

    @Override
    public void setOptionalLivingMotion(int motionId) {
        if(motionId == -1){
            this.currentOptionalLivingMotion = null;
            return;
        }
        this.currentOptionalLivingMotion = LivingMotion.ENUM_MANAGER.get(motionId);
    }
    @Override
    public boolean shouldRunWithAnim() {
        return shouldRun();
    }

    @Override
    public boolean shouldRun() {
        return getCurrentForwardSpeed() > 0.15F;
    }
    @Override
    public float getWalkSpeed() {
        return ((LivingEntityAccessor)this.getOriginal()).getSpeedAccessor();
    }
    @Override
    public void setShouldRun(boolean value) {

    }
    @Override
    public boolean shouldInterceptAi() {
        return false;
    }

    @Override
    public List<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends IdleActionAnimation>>> getIdleActionAnimations() {
        return List.of(
                new Pair<>(LivingMotions.IDLE, MobsPlusAnimations.HORSE_IDLE_ACTION_1),
                new Pair<>(LivingMotions.IDLE, MobsPlusAnimations.HORSE_IDLE_ACTION_2)
        );
    }
    @Override
    public boolean blockIdleActionAnimation() {
        return !this.getOriginal().getPassengers().isEmpty();
    }
    @Override
    public void queIdleAction(AnimationManager.AnimationAccessor<? extends IdleActionAnimation> idleAction) {
        quedIdleAction = idleAction;
    }
    @Override
    public boolean isIdleActionPlaying() {
        return this.getCurrentLivingMotion() == MobsPlusLivingMotions.IDLE_ACTION;
    }

    @Override
    public int getMinIdleActionInterval() {
        return 2;
    }

    @Override
    public int getMaxIdleActionInterval() {
        return 16;
    }

    @Override
    public AnimationManager.AnimationAccessor<? extends IdleActionAnimation> getQuedIdleAction() {
        return quedIdleAction;
    }
    @Override
    public LivingEntityPatch<?> getEntityPatch() {
        return this;
    }
}