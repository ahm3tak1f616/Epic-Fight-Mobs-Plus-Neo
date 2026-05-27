package net.kenji.epic_fight_mobs_plus;

import com.mojang.logging.LogUtils;
import net.kenji.epic_fight_mobs_plus.client.patched_renderers.WolfPatchRenderer;
import net.kenji.epic_fight_mobs_plus.compat.CompatEvents;
import net.kenji.epic_fight_mobs_plus.events.MobPatchEvents;
import net.kenji.epic_fight_mobs_plus.events.client_events.EpicFightClientEvents;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusArmatures;
import net.kenji.epic_fight_mobs_plus.gameasset.MobsPlusColliderPreset;
import net.kenji.epic_fight_mobs_plus.gameasset.animations.MobsPlusAnimations;
import net.kenji.epic_fight_mobs_plus.gameasset.mob_patches.WolfPatch;
import net.kenji.epic_fight_mobs_plus.network.MobsPlusPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import yesman.epicfight.api.utils.ExtendableEnumManager;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.model.armature.CreeperArmature;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EpicFightMobsPlus.MODID)
public class EpicFightMobsPlus {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "epic_fight_mobs_plus";


    public EpicFightMobsPlus() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(MobPatchEvents::registerPatchedEntities);
        modEventBus.addListener(MobPatchEvents::commonSetup);
        modEventBus.addListener(MobsPlusAnimations::registerAnimations);
        CompatEvents.OnInit(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobsPlusConfigCommon.SPEC, "EpicFight-MobsPlus-Common.toml");


        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(EpicFightClientEvents::registerPatchedEntityRenderers);        }
        // Register ourselves for server and other game events we are interested in
        modEventBus.register(this);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(MobsPlusPacketHandler::register);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
    @Mod.EventBusSubscriber(modid = MODID, value = Dist.DEDICATED_SERVER)
    public static class ServerForgeEvents {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void addReloadListnerEvent(final AddReloadListenerEvent event) {
            event.addListener(new MobsPlusColliderPreset());
        }
    }
}
