package net.kenji.epic_fight_mobs_plus.mixins;

import net.kenji.epic_fight_mobs_plus.MobsPlusConfigCommon;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusLivingMotions;
import net.kenji.epic_fight_mobs_plus.gameasset.animations.MobsPlusAnimations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@Mixin(value = PlayerPatch.class, remap = false)
public class PlayerPatchMixin {
    @Inject(method = "initAnimator", at = @At("TAIL"))
    public void onInitAnimator(Animator animator, CallbackInfo ci){
        PlayerPatch<?> self = (PlayerPatch<?>)(Object) this;
        if(MobsPlusConfigCommon.REPLACE_EPIC_FIGHT_MOUNT_IDLE.get())
            animator.addLivingAnimation(LivingMotions.MOUNT, MobsPlusAnimations.BIPED_MOUNT_IDLE);

        if(MobsPlusConfigCommon.USE_MOUNT_RIDE_ANIMATIONS.get()) {
            animator.addLivingAnimation(MobsPlusLivingMotions.MOUNT_FORWARD, MobsPlusAnimations.BIPED_MOUNT_RIDE_FORWARD);
            animator.addLivingAnimation(MobsPlusLivingMotions.MOUNT_BACKWARD, MobsPlusAnimations.BIPED_MOUNT_RIDE_BACKWARD);
        }
    }
}
