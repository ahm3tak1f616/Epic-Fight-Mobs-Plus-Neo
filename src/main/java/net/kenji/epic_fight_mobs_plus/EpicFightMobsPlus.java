package net.kenji.epic_fight_mobs_plus;

import com.mojang.logging.LogUtils;
import net.kenji.epic_fight_mobs_plus.api.MobPatchFactory;
import net.kenji.epic_fight_mobs_plus.client.patched_renderers.WolfPatchRenderer;
import net.kenji.epic_fight_mobs_plus.compat.CompatEvents;
import net.kenji.epic_fight_mobs_plus.events.MobPatchEvents;
import net.kenji.epic_fight_mobs_plus.events.client_events.EpicFightClientEvents;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusArmatures;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusColliderPreset;
import net.kenji.epic_fight_mobs_plus.gameasset.animations.MobsPlusAnimations;
import net.kenji.epic_fight_mobs_plus.gameasset.mob_patches.WolfPatch;
import net.kenji.epic_fight_mobs_plus.network.MobsPlusPacketHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.registry.RegisterPatchedRenderersEvent;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.model.armature.CreeperArmature;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(EpicFightMobsPlus.MODID)
public class EpicFightMobsPlus {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "epic_fight_mobs_plus";

    // Creates a creative tab with the id "epic_fight_mobs_plus:example_tab" for the example item, that is placed after the combat tab

    public EpicFightMobsPlus(ModContainer container) {
        IEventBus modEventBus = ModLoadingContext.get().getActiveContainer().getEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(MobsPlusAnimations::registerAnimations);
        CompatEvents.OnInit(modEventBus);
        container.registerConfig(ModConfig.Type.COMMON, MobsPlusConfigCommon.SPEC, "EpicFight-MobsPlus-Common.toml");
        if (FMLEnvironment.dist == Dist.CLIENT) {
            //modEventBus.addListener(EpicFightClientEvents::registerPatchedEntityRenderers);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(MobPatchEvents::registerEntityTypeArmatures);
        EpicFightEventHooks.Registry.ENTITY_PATCH.registerEvent(MobPatchEvents::registerPatchedEntities);
    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EpicFightClientEventHooks.Registry.ADD_PATCHED_ENTITY.registerEvent(EpicFightClientEvents::registerPatchedEntityRenderers);
        }
    }
    @EventBusSubscriber(modid = MODID, value = Dist.DEDICATED_SERVER)
    public static class ServerForgeEvents {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void addReloadListnerEvent(final AddReloadListenerEvent event) {
            event.addListener(new MobsPlusColliderPreset());
        }
    }
}
