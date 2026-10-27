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
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Supplier;

public record ClientQuedIdleActionSyncPacket(int entityId, ResourceLocation motionId) implements CustomPacketPayload {

    // Replace with your actual mod id path
    public static final CustomPacketPayload.Type<ClientQuedIdleActionSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EpicFightMobsPlus.MODID, "client_qued_idle_action_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientQuedIdleActionSyncPacket> STREAM_CODEC =
            StreamCodec.of(ClientQuedIdleActionSyncPacket::encode, ClientQuedIdleActionSyncPacket::decode);



    // Encode: Write data to buffer
    public static void encode(FriendlyByteBuf buf, ClientQuedIdleActionSyncPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeBoolean(packet.motionId != null);
        if (packet.motionId != null) buf.writeResourceLocation(packet.motionId);
    }

    public static ClientQuedIdleActionSyncPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        ResourceLocation motionId = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new ClientQuedIdleActionSyncPacket(entityId, motionId);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(ClientQuedIdleActionSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            executeOnClient(packet);
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void executeOnClient(ClientQuedIdleActionSyncPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Entity entity = player.level().getEntity(packet.entityId);
            if (entity == null) return;
            EntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, EntityPatch.class);
            if (entityPatch == null) return;
            if (entityPatch instanceof IAnimalMobPatch patchInterface) {
                LivingEntityPatch<?> patch = patchInterface.getEntityPatch();
                if (patch == null) return;

                // null motionId = clear signal from server
                if (packet.motionId == null) {
                    patchInterface.queIdleAction(null);
                    return;
                }

                patchInterface.queIdleAction(AnimationManager.byKey(packet.motionId));
            }
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}