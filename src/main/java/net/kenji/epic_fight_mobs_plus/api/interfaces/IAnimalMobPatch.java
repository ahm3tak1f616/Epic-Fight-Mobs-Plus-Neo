package net.kenji.epic_fight_mobs_plus.api.interfaces;

import com.mojang.datafixers.util.Pair;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleActionAnimation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public interface IAnimalMobPatch {

    LivingMotion getOptionalLivingMotion();
    void setOptionalLivingMotion(int motionId);
    boolean shouldRun();
    boolean shouldRunWithAnim();
    float getWalkSpeed();
    double getCurrentForwardSpeed();
    void setShouldRun(boolean value);
    boolean shouldInterceptAi();
    List<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends IdleActionAnimation>>> getIdleActionAnimations();
    void queIdleAction(AnimationManager.AnimationAccessor<? extends IdleActionAnimation> idleAction);
    boolean isIdleActionPlaying();
    int getMinIdleActionInterval();
    int getMaxIdleActionInterval();
    float getAnimForwardSpeed(float minSpeed, float maxSpeed);
    AnimationManager.AnimationAccessor<? extends IdleActionAnimation> getQuedIdleAction();
    LivingEntityPatch<?> getEntityPatch();
    boolean blockIdleActionAnimation();
    boolean canInteract(Player interactor, ItemStack heldItem);

}
