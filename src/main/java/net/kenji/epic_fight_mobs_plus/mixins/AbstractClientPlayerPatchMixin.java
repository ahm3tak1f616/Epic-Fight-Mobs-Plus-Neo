package net.kenji.epic_fight_mobs_plus.mixins;

import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusLivingMotions;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import org.jline.utils.Log;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.entity.ModifyPlayerLivingMotionEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

@Mixin(value = AbstractClientPlayerPatch.class, remap = false)
public abstract class AbstractClientPlayerPatchMixin {

    @Shadow
    protected abstract boolean isMoving();

    @Inject(method = "updateMotion", at = @At("HEAD"), cancellable = true)
    public void onUpdateMotion(boolean considerInaction, CallbackInfo ci){
        AbstractClientPlayerPatch<?> self = (AbstractClientPlayerPatch<?>)(Object) this;
        if (((AbstractClientPlayer)self.getOriginal()).getVehicle() instanceof PlayerRideableJumping) {
            ci.cancel();
            epicFightMobsPlusVersions$restoreNativeLogic(self, considerInaction);
        }
    }

    @Unique
    LivingMotion epicFightMobsPlusVersions$getMountLivingMotion(AbstractClientPlayerPatch self){
        Entity vehicle = self.getOriginal().getVehicle();
        Vec3 movement = vehicle.getDeltaMovement();
        Vec3 forward = vehicle.getForward();
        double forwardSpeed = movement.dot(forward); // project movement onto facing direction
        if (forwardSpeed > 0.1F)
            return MobsPlusLivingMotions.MOUNT_FORWARD;
        else if (forwardSpeed < -0.01F && vehicle.getDeltaMovement().y < 0.01)
            return MobsPlusLivingMotions.MOUNT_BACKWARD;

        return LivingMotions.MOUNT;
    }


    @Unique
    void epicFightMobsPlusVersions$restoreNativeLogic(AbstractClientPlayerPatch self, boolean considerInaction) {
        if (((AbstractClientPlayer)self.getOriginal()).getHealth() <= 0.0F) {
            self.currentLivingMotion = LivingMotions.DEATH;
        } else if (!self.getEntityState().updateLivingMotion() && considerInaction) {
            self.currentLivingMotion = LivingMotions.INACTION;
        } else if (!((AbstractClientPlayer)self.getOriginal()).isFallFlying() && !((AbstractClientPlayer)self.getOriginal()).isAutoSpinAttack()) {
            if (((AbstractClientPlayer)self.getOriginal()).getVehicle() != null) {
                if (((AbstractClientPlayer)self.getOriginal()).getVehicle() instanceof PlayerRideableJumping) {
                    self.currentLivingMotion = epicFightMobsPlusVersions$getMountLivingMotion(self);
                } else {
                    self.currentLivingMotion = LivingMotions.SIT;
                }
            } else if (((AbstractClientPlayer)self.getOriginal()).isVisuallySwimming()) {
                self.currentLivingMotion = LivingMotions.SWIM;
            } else if (((AbstractClientPlayer)self.getOriginal()).isSleeping()) {
                self.currentLivingMotion = LivingMotions.SLEEP;
            } else if (!((AbstractClientPlayer)self.getOriginal()).onGround() && ((AbstractClientPlayer)self.getOriginal()).onClimbable()) {
                self.currentLivingMotion = LivingMotions.CLIMB;
            } else if (!((AbstractClientPlayer)self.getOriginal()).getAbilities().flying) {
                ClientAnimator animator = self.getClientAnimator();
                if (((AbstractClientPlayer)self.getOriginal()).isUnderWater() && ((AbstractClientPlayer)self.getOriginal()).getY() - self.getYOld() < -0.005) {
                    self.currentLivingMotion = LivingMotions.FLOAT;
                } else if (!(((AbstractClientPlayer)self.getOriginal()).getY() - self.getYOld() < (double)-0.4F) && !self.isAirborneState()) {
                    if (this.isMoving()) {
                        if (((AbstractClientPlayer)self.getOriginal()).isCrouching()) {
                            self.currentLivingMotion = LivingMotions.SNEAK;
                        } else if (((AbstractClientPlayer)self.getOriginal()).isSprinting()) {
                            self.currentLivingMotion = LivingMotions.RUN;
                        } else {
                            self.currentLivingMotion = LivingMotions.WALK;
                        }

                        animator.baseLayer.animationPlayer.setReversed(self.dz < (double)0.0F);
                    } else {
                        animator.baseLayer.animationPlayer.setReversed(false);
                        if (((AbstractClientPlayer)self.getOriginal()).isCrouching()) {
                            self.currentLivingMotion = LivingMotions.KNEEL;
                        } else {
                            self.currentLivingMotion = LivingMotions.IDLE;
                        }
                    }
                } else {
                    self.currentLivingMotion = LivingMotions.FALL;
                }
            } else if (this.isMoving()) {
                self.currentLivingMotion = LivingMotions.CREATIVE_FLY;
            } else {
                self.currentLivingMotion = LivingMotions.CREATIVE_IDLE;
            }
        } else {
            self.currentLivingMotion = LivingMotions.FLY;
        }

        ModifyPlayerLivingMotionEvent.BaseLayer baseLayerEvent = new ModifyPlayerLivingMotionEvent.BaseLayer(self, self.currentLivingMotion, !self.getEntityState().updateLivingMotion() && considerInaction);
        EpicFightClientEventHooks.Entity.MODIFY_PLAYER_LIVING_MOTION_BASE.postWithListener(baseLayerEvent, self.getEventListener());
        self.currentLivingMotion = baseLayerEvent.getMotion();
        if (!self.getEntityState().updateLivingMotion() && considerInaction) {
            self.currentCompositeMotion = LivingMotions.NONE;
        } else {
            CapabilityItem mainhandItemCap = self.getHoldingItemCapability(InteractionHand.MAIN_HAND);
            CapabilityItem offhandItemCap = self.getHoldingItemCapability(InteractionHand.OFF_HAND);
            LivingMotion customLivingMotion = mainhandItemCap.getLivingMotion(self, InteractionHand.MAIN_HAND);
            if (customLivingMotion == null) {
                customLivingMotion = offhandItemCap.getLivingMotion(self, InteractionHand.OFF_HAND);
            }

            if (customLivingMotion != null) {
                self.currentCompositeMotion = customLivingMotion;
            } else if (((AbstractClientPlayer)self.getOriginal()).isUsingItem()) {
                UseAnim useAnim = ((AbstractClientPlayer)self.getOriginal()).getUseItem().getUseAnimation();
                if (useAnim == UseAnim.BLOCK) {
                    self.currentCompositeMotion = LivingMotions.BLOCK_SHIELD;
                } else if (useAnim == UseAnim.CROSSBOW) {
                    self.currentCompositeMotion = LivingMotions.RELOAD;
                } else if (useAnim == UseAnim.DRINK) {
                    self.currentCompositeMotion = LivingMotions.DRINK;
                } else if (useAnim == UseAnim.EAT) {
                    self.currentCompositeMotion = LivingMotions.EAT;
                } else if (useAnim == UseAnim.SPYGLASS) {
                    self.currentCompositeMotion = LivingMotions.SPECTATE;
                } else {
                    self.currentCompositeMotion = self.currentLivingMotion;
                }
            } else if (((StaticAnimation)self.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getRealAnimation().get()).isReboundAnimation()) {
                self.currentCompositeMotion = LivingMotions.SHOT;
            } else if (((AbstractClientPlayer)self.getOriginal()).swinging && ((AbstractClientPlayer)self.getOriginal()).getSleepingPos().isEmpty()) {
                self.currentCompositeMotion = LivingMotions.DIGGING;
            } else {
                self.currentCompositeMotion = self.currentLivingMotion;
            }

            ModifyPlayerLivingMotionEvent.CompositeLayer compositeLayerEvent = new ModifyPlayerLivingMotionEvent.CompositeLayer(self, self.currentCompositeMotion);
            EpicFightClientEventHooks.Entity.MODIFY_PLAYER_LIVING_MOTION_COMPOSITE.postWithListener(compositeLayerEvent, self.getEventListener());
            self.currentCompositeMotion = compositeLayerEvent.getMotion();
        }
    }
}

