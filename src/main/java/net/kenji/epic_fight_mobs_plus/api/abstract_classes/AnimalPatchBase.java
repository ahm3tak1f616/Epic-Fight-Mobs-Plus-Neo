package net.kenji.epic_fight_mobs_plus.api.abstract_classes;

import net.kenji.epic_fight_mobs_plus.api.IdleActionManager;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleActionAnimation;
import net.kenji.epic_fight_mobs_plus.api.interfaces.IAnimalMobPatch;
import net.kenji.epic_fight_mobs_plus.network.ClientOptionalLivingMotionPacket;
import net.kenji.epic_fight_mobs_plus.network.ClientPetRunPacket;
import net.kenji.epic_fight_mobs_plus.network.MobsPlusPacketHandler;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

import java.util.ArrayList;
import java.util.List;

public abstract class AnimalPatchBase<T extends Animal> extends MobPatch<T> implements IAnimalMobPatch {
    protected LivingMotion currentOptionalLivingMotion;
    public boolean shouldRun = false;

    public AnimationManager.AnimationAccessor<? extends IdleActionAnimation> quedIdleAction = null;

    public AnimalPatchBase(T entity) {
        super(entity);
    }

    public boolean computeShouldRun() {
        return false;
    }
    @Override
    public void updateMotion(boolean b) {

        currentOptionalLivingMotion = null;
        super.commonMobUpdateMotion(b);
    }

    public List<WrappedGoal> getRunningGoals(){
        List<WrappedGoal> finalWrappedGoals = new ArrayList<>();
        for(WrappedGoal wrappedGoal : this.getOriginal().goalSelector.getAvailableGoals()){
            if(wrappedGoal.isRunning()){
                finalWrappedGoals.add(wrappedGoal);
            }
        }
        return finalWrappedGoals;
    }

    @Override
    public void preTick() {
        if (!this.getOriginal().level().isClientSide()) {
            shouldRun = computeShouldRun();
            MobsPlusPacketHandler.sendToAll(new ClientPetRunPacket(getOriginal().getId(), shouldRun));
        }

        super.preTick();


        updateMotion(false);
        if (!this.getOriginal().level().isClientSide()) {
            MobsPlusPacketHandler.sendToAll(new ClientOptionalLivingMotionPacket(getOriginal().getId(), currentOptionalLivingMotion != null ? currentOptionalLivingMotion.universalOrdinal() : -1));
        }
        if(this.blockIdleActionAnimation()){
            IdleActionManager.IdleActionState state = IdleActionManager.getIdleActionState(this.getOriginal().getUUID());
            if (state.animationPlaying) {
                IdleActionManager.clearIdleActionState(this.quedIdleAction, this, state);
            }
        }
    }

    @Override
    public void postTick() {
        super.postTick();
        IdleActionManager.tickIdleAnimations(this);
    }

    @Override
    public void setShouldRun(boolean value) {
        shouldRun = value;
    }
    public float getAnimForwardSpeed(float minSpeed, float maxSpeed) {

        double forwardSpeed = getCurrentForwardSpeed();

        if (forwardSpeed < -0.05F) return -1F;

        // Get the horse's actual max walk speed attribute
        double maxWalkSpeed = getEntityPatch().getOriginal().getAttributeValue(Attributes.MOVEMENT_SPEED);

        // Normalize: 0.0 when still, 1.0 at full walk speed
        double normalized = Math.min(forwardSpeed / maxWalkSpeed, 1.0);

        // Scale between your known good minimum and maximum playback speed
        return (float)(minSpeed + normalized * (maxSpeed - minSpeed));
    }
    public double getCurrentForwardSpeed() {
        Vec3 movement = this.getOriginal().getDeltaMovement();
        Vec3 forward = this.getEntityPatch().getOriginal().getForward();
        return movement.dot(forward);
    }
    @Override
    public boolean blockIdleActionAnimation(){
        return this.getOriginal().getControllingPassenger() != null;
    }
}
