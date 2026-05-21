package net.kenji.epic_fight_mobs_plus.api;

import net.kenji.epic_fight_mobs_plus.EpicFightMobsPlus;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleActionAnimation;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleVariantAnimation;
import net.kenji.epic_fight_mobs_plus.api.interfaces.IAnimalMobPatch;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jline.utils.Log;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IdleVarientManager {
    // Single state object per entity — cleaned up on death/unload
    private static final Map<UUID, IdleVarientState> stateMap = new ConcurrentHashMap<>();

    public static class IdleVarientState {
        public LivingMotion currentLivingMotion = LivingMotions.IDLE; // -1 = needs initialization
        public Map<IdleActionAnimation, Integer> cooldowns = new ConcurrentHashMap<> (); // remaining cooldown per animation
    }

    public static IdleVarientState getIdleVarientState(UUID Uuid){
        return stateMap.computeIfAbsent(Uuid, k -> new IdleVarientState());

    }
    public static void rollRandomIdleVariant(LivingEntityPatch<?> patch){
        List<IdleVariantAnimation> list = new ArrayList<>();
        patch.getAnimator().getLivingAnimations().forEach((motion, anim) ->{
            if(anim.get() instanceof IdleVariantAnimation idleVariantAnimation){
                list.add(idleVariantAnimation);
            }
        });

        getIdleVarientState(patch.getOriginal().getUUID()).currentLivingMotion = weightedRandom(list, patch.getOriginal().getRandom());
    }

    public static void tickIdleVariantAnimations(IAnimalMobPatch patchInterface) {
        if (patchInterface.getEntityPatch().isLogicalClient()) return;

        UUID id = patchInterface.getEntityPatch().getOriginal().getUUID();
        IdleVarientState state = getIdleVarientState(id);
        tickCooldowns(state);
    }

    private static void tickCooldowns(IdleVarientState state) {
        state.cooldowns.replaceAll((anim, ticks) -> ticks - 1);
        state.cooldowns.entrySet().removeIf(e -> e.getValue() <= 0);
    }
    private static LivingMotion weightedRandom(List<IdleVariantAnimation> candidates, RandomSource random) {
        if(candidates.isEmpty()) return LivingMotions.IDLE;
        float totalWeight = (float) candidates.stream().mapToDouble(IdleVariantAnimation::getWeight).sum();
        float roll = random.nextFloat() * totalWeight;
        for (IdleVariantAnimation anim : candidates) {
            roll -= anim.getWeight();
            if (roll <= 0) return anim.getAssociatedMotion();
        }

        return candidates.get(candidates.size() - 1).getAssociatedMotion();
    }
}
