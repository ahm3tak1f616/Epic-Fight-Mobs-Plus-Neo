package net.kenji.epic_fight_mobs_plus.compat.doggy_talents_next.patches;

import com.mojang.datafixers.util.Pair;
import doggytalents.common.entity.Dog;
import doggytalents.common.entity.ai.DogFollowOwnerGoal;
import doggytalents.common.item.TreatItem;
import net.kenji.epic_fight_mobs_plus.api.IdleActionManager;
import net.kenji.epic_fight_mobs_plus.api.abstract_classes.AnimalPatchBase;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleActionAnimation;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusLivingMotions;
import net.kenji.epic_fight_mobs_plus.gameasset.animations.MobsPlusAnimations;
import net.kenji.epic_fight_mobs_plus.mixins.accessors.DogAccessor;
import net.kenji.epic_fight_mobs_plus.mixins.accessors.DogAiManagerAccessor;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEvent;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

import java.util.List;

public class DogPatch<D extends Dog> extends AnimalPatchBase<Dog> {

    public boolean isFollowingOwner = false;
    public static int MAX_COUNTER = 20;
    public int isFollowingOwnerCounter = 20;


    @Override
    public void tick(LivingEvent.LivingTickEvent event) {
        super.tick(event);
    }

    @Override
    public boolean computeShouldRun() {
        boolean followGoalActive = false;

        for (WrappedGoal wrappedGoal : ((DogAiManagerAccessor)this.getOriginal().dogAi).getGoals()) {
            if (wrappedGoal.getGoal() instanceof DogFollowOwnerGoal || this.getOriginal().isDogFollowingSomeone()) {
                followGoalActive = true;

                isFollowingOwnerCounter = MAX_COUNTER;
                this.getOriginal().getPersistentData().putBoolean("is_following", true);
            }
        }

        // Decrement counter and update flag
        if (isFollowingOwnerCounter > 0) {
            isFollowingOwnerCounter--;
            isFollowingOwner = followGoalActive || isFollowingOwnerCounter > 0;
        } else {
            isFollowingOwner = false;
            this.getOriginal().getPersistentData().putBoolean("is_following", false);
        }


        return isFollowingOwner ||  this.getOriginal().getPersistentData().getBoolean("is_following") || this.getOriginal().getTarget() != null;
    }

    @Override
    public void updateMotion(boolean b) {
        if (((DogAccessor)this.getOriginal()).getIsShaking() ||
                this.getOriginal().isDefeated()) {

            IdleActionManager.IdleActionState state = IdleActionManager.getIdleActionState(this.getOriginal().getUUID());
            if (state.animationPlaying) {
                IdleActionManager.clearIdleActionState(this.quedIdleAction, this, state);
            }
        }
        if (this.getOriginal().isInSittingPose()) {
            this.currentLivingMotion = LivingMotions.SIT;
            this.currentCompositeMotion = LivingMotions.SIT;
            currentOptionalLivingMotion = currentLivingMotion;
            return;
        }
        if (((DogAccessor)this.getOriginal()).getIsShaking()) {
            this.currentLivingMotion = MobsPlusLivingMotions.WOLF_SHAKE_OFF;
            this.currentCompositeMotion = MobsPlusLivingMotions.WOLF_SHAKE_OFF;
            currentOptionalLivingMotion = currentLivingMotion;
            return;
        }
        if(this.getOriginal().isDefeated()){
            this.currentLivingMotion = MobsPlusLivingMotions.DYING;
            this.currentCompositeMotion = MobsPlusLivingMotions.DYING;
            currentOptionalLivingMotion = currentLivingMotion;
            return;
        }
        super.updateMotion(b);
    }

    @Override
    public boolean blockIdleActionAnimation() {
        if(this.getOriginal().getMode().shouldFollowOwner() || this.getOriginal().isAggressive()){
            return true;
        }
        return super.blockIdleActionAnimation();
    }

    @Override
    public LivingMotion getCurrentLivingMotion() {
        return this.currentLivingMotion;
    }
    @Override
    protected void initAI() {
        this.original.goalSelector.addGoal(
                1,
                new AnimatedAttackGoal<>(this, new CombatBehaviors.Builder<>().newBehaviorSeries(CombatBehaviors.BehaviorSeries.builder().weight(10).nextBehavior(CombatBehaviors.Behavior.builder().animationBehavior(MobsPlusAnimations.WOLF_ATTACK_1).withinDistance(0.1F, 1.8F))).build(this))
        );
        this.original.goalSelector.addGoal(1, new TargetChasingGoal(this,
                (PathfinderMob) this.original, 1.0F, false));
    }

    @Override
    protected void initAnimator(Animator animator) {
        super.initAnimator(animator);

        animator.addLivingAnimation(LivingMotions.IDLE, MobsPlusAnimations.WOLF_IDLE);
        animator.addLivingAnimation(MobsPlusLivingMotions.IDLE_VAR2, MobsPlusAnimations.WOLF_IDLE_VAR1);
        animator.addLivingAnimation(MobsPlusLivingMotions.IDLE_VAR3, MobsPlusAnimations.WOLF_IDLE_VAR2);
        animator.addLivingAnimation(MobsPlusLivingMotions.IDLE_VAR4, MobsPlusAnimations.WOLF_IDLE_VAR3);
        animator.addLivingAnimation(LivingMotions.WALK, MobsPlusAnimations.WOLF_WALK);
        animator.addLivingAnimation(LivingMotions.CHASE, MobsPlusAnimations.WOLF_RUN);
        animator.addLivingAnimation(LivingMotions.SIT, MobsPlusAnimations.WOLF_SITTING);
        animator.addLivingAnimation(LivingMotions.DEATH, MobsPlusAnimations.WOLF_DEATH);

        animator.addLivingAnimation(MobsPlusLivingMotions.WOLF_SHAKE_OFF, MobsPlusAnimations.WOLF_SHAKE);
        animator.addLivingAnimation(MobsPlusLivingMotions.DYING, MobsPlusAnimations.WOLF_DYING);

    }

    @Override
    public AssetAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        return MobsPlusAnimations.WOLF_IDLE;
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
    public boolean shouldRun() {
       // Log.info("Logging Should Run For Dog: " + shouldRun);
        return shouldRun || this.getOriginal().isDogFollowingSomeone();
    }

    @Override
    public boolean shouldRunWithAnim() {
        return shouldRun() && getCurrentForwardSpeed() > 0.1F;
    }

    @Override
    public void setShouldRun(boolean value) {
        shouldRun = value;
    }
    @Override
    public boolean shouldInterceptAi() {
        return false;
    }

    @Override
    public List<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends IdleActionAnimation>>> getIdleActionAnimations() {
        return List.of(
                new Pair<>(LivingMotions.IDLE, MobsPlusAnimations.WOLF_IDLE_ACTION_1),
                new Pair<>(LivingMotions.IDLE, MobsPlusAnimations.WOLF_IDLE_ACTION_2),
                new Pair<>(LivingMotions.SIT, MobsPlusAnimations.WOLF_SIT_ACTION_1),
                new Pair<>(LivingMotions.SIT, MobsPlusAnimations.WOLF_SIT_ACTION_2),
                new Pair<>(LivingMotions.SIT, MobsPlusAnimations.WOLF_SIT_ACTION_3),
                new Pair<>(LivingMotions.SIT, MobsPlusAnimations.WOLF_SIT_ACTION_4)
                );
    }

    @Override
    public void queIdleAction(AnimationManager.AnimationAccessor<? extends IdleActionAnimation> idleAction) {
        quedIdleAction = idleAction;
    }
    @Override
    public int getMinIdleActionInterval() {
        return 2;
    }

    @Override
    public int getMaxIdleActionInterval() {
        return 12;
    }
    @Override
    public boolean isIdleActionPlaying() {
        return this.getCurrentLivingMotion() == MobsPlusLivingMotions.IDLE_ACTION;
    }
    @Override
    public AnimationManager.AnimationAccessor<? extends IdleActionAnimation> getQuedIdleAction(){
        return quedIdleAction;
    }

    @Override
    public float getWalkSpeed() {
        return this.getOriginal().getUrgentSpeedModifier() / 2;
    }

    @Override
    public LivingEntityPatch<?> getEntityPatch() {
        return this;
    }

    @Override
    public boolean canInteract(Player interactor, ItemStack heldItem) {
        if(this.getOriginal().isTame() && heldItem.getItem() instanceof TreatItem)
            return true;
        if((!this.getOriginal().isTame() && heldItem.getItem() == Items.BONE) || super.isFoodAcceptable(interactor, heldItem))
            return true;
        return this.getOriginal().isTame() && this.getOriginal().getOwner() == interactor;
    }
}
