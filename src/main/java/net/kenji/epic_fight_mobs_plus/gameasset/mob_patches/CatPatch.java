package net.kenji.epic_fight_mobs_plus.gameasset.mob_patches;

import com.mojang.datafixers.util.Pair;
import net.kenji.epic_fight_mobs_plus.api.IdleActionManager;
import net.kenji.epic_fight_mobs_plus.api.abstract_classes.AnimalPatchBase;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleActionAnimation;
import net.kenji.epic_fight_mobs_plus.api.interfaces.IAnimalMobPatch;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusLivingMotions;
import net.kenji.epic_fight_mobs_plus.gameasset.animations.MobsPlusAnimations;
import net.kenji.epic_fight_mobs_plus.goals.ChasePassiveMobGoal;
import net.kenji.epic_fight_mobs_plus.mixins.accessors.LivingEntityAccessor;
import net.kenji.epic_fight_mobs_plus.network.ClientOptionalLivingMotionPacket;
import net.kenji.epic_fight_mobs_plus.network.ClientPetRunPacket;
import net.kenji.epic_fight_mobs_plus.network.MobsPlusPacketHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

import java.util.List;
import java.util.function.Predicate;

public class CatPatch<H extends Cat> extends AnimalPatchBase<Cat> {


    @Override
    public void tick(LivingEvent.LivingTickEvent event) {
        super.tick(event);
    }

    public boolean isFollowingOwner = false;
    public static int MAX_COUNTER = 20;
    public int isFollowingOwnerCounter = 20;
    @Override
    public boolean computeShouldRun() {
        boolean followGoalActive = false;

        for (WrappedGoal wrappedGoal : this.getOriginal().goalSelector.getRunningGoals().toList()) {
            if (wrappedGoal.getGoal() instanceof FollowOwnerGoal || wrappedGoal.getGoal() instanceof AvoidEntityGoal<?>) {
                followGoalActive = true;
                isFollowingOwnerCounter = MAX_COUNTER;
                this.getOriginal().getPersistentData().putBoolean("is_running", true);
            }
        }

        // Decrement counter and update flag
        if (isFollowingOwnerCounter > 0) {
            isFollowingOwnerCounter--;
            isFollowingOwner = followGoalActive || isFollowingOwnerCounter > 0;
        } else {
            isFollowingOwner = false;
            this.getOriginal().getPersistentData().putBoolean("is_running", false);
        }

        return isFollowingOwner ||  this.getOriginal().getPersistentData().getBoolean("is_following");
    }

    @Override
    public AssetAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
        return null;
    }

    @Override
    public void updateMotion(boolean b) {
        if (this.getOriginal().isLying()) {
            IdleActionManager.IdleActionState state = IdleActionManager.getIdleActionState(this.getOriginal().getUUID());
            if (state.animationPlaying) {
                IdleActionManager.clearIdleActionState(this.quedIdleAction, this, state);
            }
        }
        if (this.getOriginal().isLying()) {
            this.currentLivingMotion = MobsPlusLivingMotions.CAT_LAY;
            this.currentCompositeMotion = MobsPlusLivingMotions.CAT_LAY;
            currentOptionalLivingMotion = currentLivingMotion;
            return;
        }
        if (this.getOriginal().isInSittingPose()) {
            this.currentLivingMotion = LivingMotions.SIT;
            this.currentCompositeMotion = LivingMotions.SIT;
            currentOptionalLivingMotion = currentLivingMotion;
            return;
        }

        super.updateMotion(b);
    }
    @Override
    protected void initAI() {
        this.original.goalSelector.addGoal(
                1,
                new AnimatedAttackGoal<>(this, new CombatBehaviors.Builder<>().newBehaviorSeries(CombatBehaviors.BehaviorSeries.builder().weight(10).nextBehavior(CombatBehaviors.Behavior.builder().animationBehavior(MobsPlusAnimations.CAT_ATTACK).withinDistance(0.1F, 1.75F))).build(this))
        );
        this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, (PathfinderMob)this.original, (double)1.0F, false));

        this.original.targetSelector.addGoal(2, new ChasePassiveMobGoal(this, this.getOriginal(), Rabbit.class, (Predicate<LivingEntity>)null,1.0D, false));
        this.original.targetSelector.addGoal(2, new ChasePassiveMobGoal(this, this.getOriginal(), Turtle.class, Turtle.BABY_ON_LAND_SELECTOR,1.0D, false));

    }
    @Override
    protected void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, MobsPlusAnimations.CAT_IDLE);
        animator.addLivingAnimation(MobsPlusLivingMotions.IDLE_VAR2, MobsPlusAnimations.CAT_IDLE_VAR1);
        animator.addLivingAnimation(MobsPlusLivingMotions.IDLE_VAR3, MobsPlusAnimations.CAT_IDLE_VAR2);
        animator.addLivingAnimation(MobsPlusLivingMotions.IDLE_VAR4, MobsPlusAnimations.CAT_IDLE_VAR3);
        animator.addLivingAnimation(LivingMotions.WALK, MobsPlusAnimations.CAT_WALK);
        animator.addLivingAnimation(LivingMotions.CHASE, MobsPlusAnimations.CAT_RUN);
        animator.addLivingAnimation(LivingMotions.SIT, MobsPlusAnimations.CAT_SITTING);
        animator.addLivingAnimation(MobsPlusLivingMotions.CAT_LAY, MobsPlusAnimations.CAT_LAYING);

        animator.addLivingAnimation(LivingMotions.DEATH, MobsPlusAnimations.CAT_DEATH);

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
       if(!this.getOriginal().isSteppingCarefully()) {
           return shouldRun || this.getOriginal().getTarget() != null;
       }
        return false;
    }

    @Override
    public float getWalkSpeed() {
        return ((LivingEntityAccessor)this.getOriginal()).getSpeedAccessor() / 2;
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
                new Pair<>(LivingMotions.SIT, MobsPlusAnimations.CAT_SIT_ACTION_1),
                new Pair<>(LivingMotions.SIT, MobsPlusAnimations.CAT_SIT_ACTION_2),
                new Pair<>(LivingMotions.SIT, MobsPlusAnimations.CAT_SIT_ACTION_3)
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
    public LivingEntityPatch<?> getEntityPatch() {
        return this;
    }

    @Override
    public boolean canInteract(Player interactor, ItemStack heldItem) {
        if(super.isFoodAcceptable(interactor, heldItem))
            return true;
        return this.getOriginal().isTame() && this.getOriginal().getOwner() == interactor;
    }
}