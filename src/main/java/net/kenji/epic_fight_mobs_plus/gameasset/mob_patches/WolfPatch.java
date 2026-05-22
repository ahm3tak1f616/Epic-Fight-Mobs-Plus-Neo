package net.kenji.epic_fight_mobs_plus.gameasset.mob_patches;

import com.mojang.datafixers.util.Pair;
import net.kenji.epic_fight_mobs_plus.api.IdleActionManager;
import net.kenji.epic_fight_mobs_plus.api.abstract_classes.AnimalPatchBase;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleActionAnimation;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusLivingMotions;
import net.kenji.epic_fight_mobs_plus.gameasset.animations.MobsPlusAnimations;
import net.kenji.epic_fight_mobs_plus.goals.ChasePassiveMobGoal;
import net.kenji.epic_fight_mobs_plus.mixins.accessors.LivingEntityAccessor;
import net.kenji.epic_fight_mobs_plus.mixins.accessors.WolfAccessor;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

import java.util.List;

public class WolfPatch<W extends TamableAnimal> extends AnimalPatchBase<Wolf> {

    public boolean isFollowingOwner = false;
    public static int MAX_COUNTER = 20;
    public int isFollowingOwnerCounter = 20;

    public WolfPatch(Wolf entity) {
        super(entity);
    }


    @Override
    public boolean computeShouldRun() {
        boolean followGoalActive = false;

        for (WrappedGoal wrappedGoal : this.getRunningGoals()) {
            if (wrappedGoal.getGoal() instanceof FollowOwnerGoal) {
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
        if (((WolfAccessor)this.getOriginal()).getIsShaking()) {

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
        if (((WolfAccessor)this.getOriginal()).getIsShaking()) {
            this.currentLivingMotion = MobsPlusLivingMotions.WOLF_SHAKE_OFF;
            this.currentCompositeMotion = MobsPlusLivingMotions.WOLF_SHAKE_OFF;
            currentOptionalLivingMotion = currentLivingMotion;
            return;
        }
        super.updateMotion(b);
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
        this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, (PathfinderMob)this.original, (double)1.0F, false));
        this.original.targetSelector.addGoal(5, new ChasePassiveMobGoal(this, this.getOriginal(), Animal.class, Wolf.PREY_SELECTOR,1.0D, false));
        this.original.targetSelector.addGoal(5, new ChasePassiveMobGoal(this, this.getOriginal(), Turtle.class, Turtle.BABY_ON_LAND_SELECTOR,1.0D, false));

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

    }
    @Override
    public boolean blockIdleActionAnimation() {
        if(this.getOriginal().isAggressive()){
            return true;
        }
        return super.blockIdleActionAnimation();
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
    public boolean shouldRunWithAnim() {
       return shouldRun() && (getCurrentForwardSpeed() > this.getWalkSpeed());
    }
    @Override
    public boolean shouldRun() {

        return shouldRun;
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
    public boolean isIdleActionPlaying() {
        return this.getCurrentLivingMotion() == MobsPlusLivingMotions.IDLE_ACTION;
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
    public AnimationManager.AnimationAccessor<? extends IdleActionAnimation> getQuedIdleAction() {
        return quedIdleAction;
    }
    @Override
    public float getWalkSpeed() {
        return ((LivingEntityAccessor)this.getOriginal()).getSpeedAccessor() / 2;
    }
    @Override
    public LivingEntityPatch<?> getEntityPatch() {
        return this;
    }

    @Override
    public boolean canInteract(Player interactor, ItemStack heldItem) {
        if((!this.getOriginal().isTame() && heldItem.getItem() == Items.BONE) || super.isFoodAcceptable(interactor, heldItem))
            return true;
        return this.getOriginal().isTame() && this.getOriginal().getOwner() == interactor;
    }
}