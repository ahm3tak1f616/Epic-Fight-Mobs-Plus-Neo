package net.kenji.epic_fight_mobs_plus.mixins.animal_mixins;

import net.kenji.epic_fight_mobs_plus.gameasset.mob_patches.WolfPatch;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(Wolf.class)
public class WolfMixin {
    @Redirect(
            method = "registerGoals",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;addGoal(ILnet/minecraft/world/entity/ai/goal/Goal;)V"
            )
    )
    private void redirectAddGoal(GoalSelector selector, int priority, Goal goal) {

        if (goal instanceof LeapAtTargetGoal ||
                goal instanceof MeleeAttackGoal) {
            return; // skip adding
        }

        selector.addGoal(priority, goal);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    public void onInteract(Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir){
        Wolf self = (Wolf) (Object)this;
        self.setNoAi(false);
    }
    @Inject(method = "mobInteract", at = @At("TAIL"), cancellable = true)
    public void onInteractTail(Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir){
        Wolf self = (Wolf) (Object)this;
    }

}
