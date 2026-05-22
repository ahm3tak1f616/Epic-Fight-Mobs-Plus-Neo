package net.kenji.epic_fight_mobs_plus.api;

import com.mojang.datafixers.util.Pair;
import net.kenji.epic_fight_mobs_plus.EpicFightMobsPlus;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleActionAnimation;
import net.kenji.epic_fight_mobs_plus.api.interfaces.IAnimalMobPatch;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusLivingMotions;
import net.kenji.epic_fight_mobs_plus.gameasset.mob_patches.CatPatch;
import net.kenji.epic_fight_mobs_plus.network.ClientIdleActionSyncPacket;
import net.kenji.epic_fight_mobs_plus.network.ClientQuedIdleActionSyncPacket;
import net.kenji.epic_fight_mobs_plus.network.MobsPlusPacketHandler;
import net.kenji.epic_fight_mobs_plus.network.ServerIdleActionPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jline.utils.Log;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class IdleActionManager {
    // Single state object per entity — cleaned up on death/unload
    private static final Map<UUID, IdleActionState> stateMap = new ConcurrentHashMap<>();

    public static class IdleActionState {
        public int ticksUntilNextAction = -1; // -1 = needs initialization
        public boolean animationPlaying = false;
        public float initialRotation = -1;
        public float currentElapsedTime = -1;
        public float currentQuedTotalTime = -1;
        public Map<IdleActionAnimation, Integer> cooldowns = new ConcurrentHashMap<>(); // remaining cooldown per animation
    }

    public static IdleActionState getIdleActionState(UUID Uuid){
        return stateMap.computeIfAbsent(Uuid, k -> new IdleActionState());
    }

    public static void tickIdleAnimations(IAnimalMobPatch iMobPatch) {
        LivingEntityPatch<?> livingEntityPatch = iMobPatch.getEntityPatch();
        UUID id = iMobPatch.getEntityPatch().getOriginal().getUUID();
        IdleActionState state = getIdleActionState(id);

        Animator animator = iMobPatch.getEntityPatch().getAnimator();
        if (animator instanceof ClientAnimator clientAnimator) {
            AnimationPlayer animPlayer = clientAnimator.getPlayerFor(null);
            if (animPlayer != null) {
                MobsPlusPacketHandler.sendToServer(new ServerIdleActionPacket(livingEntityPatch.getOriginal().getUUID(), null, animPlayer.getElapsedTime(), animPlayer.getAnimation().get().getTotalTime()));
            }
        }

        if (livingEntityPatch.isLogicalClient()) return;

        // Only tick cooldowns when no animation is playing
        if (!state.animationPlaying) {
            tickCooldowns(state);
        }
        List<LivingMotion> motions = new ArrayList<>();
        LivingMotion currentMotion = livingEntityPatch.getCurrentLivingMotion();
        boolean isValidLivingMotion = false;
        iMobPatch.getIdleActionAnimations().forEach(pair -> {
            if (!motions.contains(pair.getFirst())) {
                motions.add(pair.getFirst());
            }
        });

        if(currentMotion instanceof MobsPlusLivingMotions mobsPlusLivingMotions){
            if(mobsPlusLivingMotions.parentMotion != null && motions.contains(mobsPlusLivingMotions.parentMotion)){
                isValidLivingMotion = true;
            }
        }
        else if(motions.contains(currentMotion)){
            isValidLivingMotion = true;
        }


        if (isValidLivingMotion && !iMobPatch.blockIdleActionAnimation()) {
            if (!state.animationPlaying) {
                if (iMobPatch.getQuedIdleAction() == null) {
                    handleIdleActionAssign(iMobPatch, state);
                }
                else {
                    if (animator instanceof ServerAnimator serverAnimator) {
                        AnimationPlayer animPlayer = serverAnimator.animationPlayer;
                        if (animPlayer != null) {

                            if (state.currentElapsedTime >= state.currentQuedTotalTime - 0.1F || iMobPatch.getQuedIdleAction().get().isShouldPlayInstantly()) {

                                playQuedIdleAction(iMobPatch, iMobPatch.getQuedIdleAction().get());
                            }
                        }
                    }
                }
            } else {
                handleAnimationPlay(iMobPatch, state);
            }
        } else {
            if (state.animationPlaying) {
                AssetAccessor<? extends IdleActionAnimation> queued = iMobPatch.getQuedIdleAction();
                clearIdleActionState(queued, iMobPatch, state);
                // Send null to tell clients to clear
                MobsPlusPacketHandler.sendToAll(new ClientIdleActionSyncPacket(
                        livingEntityPatch.getOriginal().getId(), null));
            }
        }
    }


    private static void tickCooldowns(IdleActionState state) {
        state.cooldowns.replaceAll((anim, ticks) -> ticks - 1);
        state.cooldowns.entrySet().removeIf(e -> e.getValue() <= 0);
    }

    private static void handleIdleActionAssign(IAnimalMobPatch patchInterface, IdleActionState state) {
        List<Pair<LivingMotion, AnimationManager.AnimationAccessor<? extends IdleActionAnimation>>> animations = patchInterface.getIdleActionAnimations();
        if (animations.isEmpty()) return;
        List<AnimationManager.AnimationAccessor<? extends IdleActionAnimation>> finalAnims = new ArrayList<>();
        animations.forEach(pair -> {
            LivingMotion currentMotion = patchInterface.getEntityPatch().getCurrentLivingMotion();

            if(currentMotion instanceof MobsPlusLivingMotions mobPlusMotion){
                if(mobPlusMotion.parentMotion != null){
                    currentMotion = mobPlusMotion.parentMotion;
                }
            }

            if(pair.getFirst() == currentMotion){
                finalAnims.add(pair.getSecond());
            }
        });

        Log.info("Logging Anim Chose1!");

        if (finalAnims.isEmpty()) return;

        // Filter to only IdleActionAnimation instances (ignore plain StaticAnimations)
        List<IdleActionAnimation> idleAnims = finalAnims.stream()
                .map(AssetAccessor::get) // however you resolve the accessor
                .filter(a -> a instanceof IdleActionAnimation)
                .map(a -> (IdleActionAnimation) a)
                .filter(a -> !state.cooldowns.containsKey(a)) // skip animations on cooldown
                .toList();

        if (idleAnims.isEmpty()) return;

        Log.info("Logging Anim Chose2!");
        if (state.ticksUntilNextAction < 0) {
            // Pick wait time from the highest-priority eligible animation
            IdleActionAnimation representative = highestPriority(idleAnims);
            RandomSource random = patchInterface.getEntityPatch().getOriginal().getRandom();
            state.ticksUntilNextAction = (int)Mth.randomBetween(random,
                    representative.getMinWaitTicks(), representative.getMaxWaitTicks());
            return;
        }
        Log.info("Logging Anim Chose3!");
        if (state.ticksUntilNextAction > 0) {
            state.ticksUntilNextAction--;
            return;
        }

        // Time to pick — filter to highest priority tier only, then weighted random
        int maxPriority = idleAnims.stream().mapToInt(IdleActionAnimation::getPlayPriority).max().orElse(0);
        List<IdleActionAnimation> candidates = idleAnims.stream()
                .filter(a -> a.getPlayPriority() == maxPriority)
                .toList();


        IdleActionAnimation chosen = weightedRandom(candidates, patchInterface.getEntityPatch().getOriginal().getRandom());
        patchInterface.queIdleAction(chosen.getAccessor());
        MobsPlusPacketHandler.sendToAll(new ClientQuedIdleActionSyncPacket(patchInterface.getEntityPatch().getOriginal().getId(), chosen.getLocation()));
    }

    public static void playQuedIdleAction(IAnimalMobPatch patchInterface, IdleActionAnimation chosen){
        UUID id = patchInterface.getEntityPatch().getOriginal().getUUID();
        IdleActionState state = getIdleActionState(id);

        Log.info("Playing Animation!");
        patchInterface.getEntityPatch().playAnimationSynchronized(chosen.getAccessor(), 0.1F);


        state.animationPlaying = true;
        state.ticksUntilNextAction = -1;
        state.initialRotation = patchInterface.getEntityPatch().getOriginal().getYRot();
    }

    private static void handleAnimationPlay(IAnimalMobPatch patchInterface, IdleActionState state) {
        LivingEntity entity = patchInterface.getEntityPatch().getOriginal();

        if (entity instanceof Mob mob) {
            // Disable AI so goals don't fight the freeze
            mob.setNoAi(true);
        }


        entity.setDeltaMovement(new Vec3(0, entity.getDeltaMovement().y, 0));
        entity.zza = 0;
        entity.xxa = 0; // strafe input too
    }

    public static void clearIdleActionState(AssetAccessor<? extends IdleActionAnimation> idleAnim, IAnimalMobPatch patchInterface, IdleActionState state) {
        LivingEntity entity = patchInterface.getEntityPatch().getOriginal();
        if (!state.animationPlaying) {
            if(entity instanceof Mob mob){
                mob.setNoAi(false);
            }
            return;
        }


        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
            mob.getNavigation().stop();
            mob.getNavigation().recomputePath();
        }

        if (idleAnim != null) {
            state.cooldowns.forEach((k, cooldown) -> state.cooldowns.put(k, k.getCooldownTicks()));
        }
        stopIdleAnimation(patchInterface);


        patchInterface.queIdleAction(null);
        state.animationPlaying = false;
        state.initialRotation = -1;
        state.ticksUntilNextAction = -1;
    }

    public static void stopIdleAnimation(IAnimalMobPatch iAnimalMobPatch){
        AnimationPlayer animPLayer = iAnimalMobPatch.getEntityPatch().getAnimator().getPlayerFor(null);
        if(animPLayer != null) {
            AtomicBoolean shouldStopPlaying = new AtomicBoolean(false);
            iAnimalMobPatch.getIdleActionAnimations().forEach(pair -> {
                if (animPLayer.getAnimation().get() == pair.getSecond().get()) {
                    shouldStopPlaying.set(true);
                }
            });

            if (shouldStopPlaying.get()) {
                iAnimalMobPatch.getEntityPatch().stopPlaying(animPLayer.getAnimation().get().getRealAnimation());
            }
        }
    }


    public static void stopAnimationState(IAnimalMobPatch iAnimalMobPatch, IdleActionState state){
        if (iAnimalMobPatch.getEntityPatch().getOriginal() instanceof Mob mob) {
            mob.setNoAi(false);
            mob.getNavigation().stop();
            mob.getNavigation().recomputePath();
        }
        iAnimalMobPatch.queIdleAction(null);
        state.animationPlaying = false;
        state.initialRotation = -1;
        state.ticksUntilNextAction = -1;
    }

    private static IdleActionAnimation highestPriority(List<IdleActionAnimation> anims) {
        return anims.stream().max(Comparator.comparingInt(IdleActionAnimation::getPlayPriority)).orElseThrow();
    }

    private static IdleActionAnimation weightedRandom(List<IdleActionAnimation> candidates, RandomSource random) {
        float totalWeight = (float) candidates.stream().mapToDouble(IdleActionAnimation::getWeight).sum();
        float roll = random.nextFloat() * totalWeight;
        for (IdleActionAnimation anim : candidates) {
            roll -= anim.getWeight();
            if (roll <= 0) return anim;
        }
        return candidates.get(candidates.size() - 1); // fallback
    }

    @EventBusSubscriber(modid = EpicFightMobsPlus.MODID)
    public static class Events {

        @SubscribeEvent
        public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
            if (!(event.getTarget() instanceof LivingEntity livingEntity)) return;
            Player interactor = event.getEntity();
            ItemStack heldItem = interactor.getInventory().getSelected();

            UUID id = livingEntity.getUUID();
            IdleActionState state = stateMap.get(id);
            if (state == null || !state.animationPlaying) return;

            LivingEntityPatch<?> patch = MobsPlusCommonHandler.getLivingEntityPatch(livingEntity);
            if (patch instanceof IAnimalMobPatch iMobPatch) {
                if (iMobPatch.canInteract(interactor, heldItem)) {
                    AssetAccessor<? extends IdleActionAnimation> queued = iMobPatch.getQuedIdleAction();
                    clearIdleActionState(queued, iMobPatch, state);
                    MobsPlusPacketHandler.sendToAll(new ClientIdleActionSyncPacket(
                            livingEntity.getId(), null));
                }
            }
        }
        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            stateMap.remove(event.getEntity().getUUID());
        }
    }
}
