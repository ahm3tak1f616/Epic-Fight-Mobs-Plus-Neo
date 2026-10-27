package net.kenji.epic_fight_mobs_plus.network;

import net.kenji.epic_fight_mobs_plus.EpicFightMobsPlus;
import net.kenji.epic_fight_mobs_plus.api.interfaces.IAnimalMobPatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Objects;
import java.util.function.Supplier;

public record ClientPlayAnimationPacket(int entityId, ResourceLocation motionId) implements CustomPacketPayload {

    // Replace with your actual mod id path
    public static final CustomPacketPayload.Type<ClientPlayAnimationPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EpicFightMobsPlus.MODID, "client_play_animation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientPlayAnimationPacket> STREAM_CODEC =
            StreamCodec.of(ClientPlayAnimationPacket::encode, ClientPlayAnimationPacket::decode);


    // Encode: Write data to buffer
    public static void encode(FriendlyByteBuf buf, ClientPlayAnimationPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeBoolean(packet.motionId != null);
        if (packet.motionId != null) buf.writeResourceLocation(packet.motionId);
    }

    public static ClientPlayAnimationPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        ResourceLocation motionId = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new ClientPlayAnimationPacket(entityId, motionId);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(ClientPlayAnimationPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT)
                executeOnClient(packet);
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void executeOnClient(ClientPlayAnimationPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        Entity entity = player.level().getEntity(packet.entityId);
        if (entity == null) return;

        EntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, EntityPatch.class);
        if (entityPatch == null) return;
        if (entityPatch instanceof IAnimalMobPatch patchInterface) {
            LivingEntityPatch<?> patch = patchInterface.getEntityPatch();
            if (patch == null) return;

            AnimationManager.AnimationAccessor<StaticAnimation> anim = AnimationManager.byKey(packet.motionId);
            if (anim != null)
                if (Objects.requireNonNull(patch.getAnimator().getPlayerFor(null)).getAnimation().get() != anim.get())
                    patch.getAnimator().playAnimation(anim, 0.05F);
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}