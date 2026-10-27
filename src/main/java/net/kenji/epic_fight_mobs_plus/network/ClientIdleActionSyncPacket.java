package net.kenji.epic_fight_mobs_plus.network;

import net.kenji.epic_fight_mobs_plus.EpicFightMobsPlus;
import net.kenji.epic_fight_mobs_plus.api.interfaces.IAnimalMobPatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public record ClientIdleActionSyncPacket(int entityId, ResourceLocation motionId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientIdleActionSyncPacket> TYPE =
            new CustomPacketPayload.Type<ClientIdleActionSyncPacket>(ResourceLocation.fromNamespaceAndPath(EpicFightMobsPlus.MODID, "client_idle_sync"));

    public static final StreamCodec<FriendlyByteBuf, ClientIdleActionSyncPacket> STREAM_CODEC =
            StreamCodec.of(ClientIdleActionSyncPacket::encode, ClientIdleActionSyncPacket::decode);

    // Encode: Write data to buffer
    public static void encode(FriendlyByteBuf buf, ClientIdleActionSyncPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeBoolean(packet.motionId != null);
        if (packet.motionId != null) buf.writeResourceLocation(packet.motionId);
    }

    public static ClientIdleActionSyncPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        ResourceLocation motionId = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new ClientIdleActionSyncPacket(entityId, motionId);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(ClientIdleActionSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                executeOnClient(packet);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void executeOnClient(ClientIdleActionSyncPacket packet) {
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

            // null motionId = clear signal from server
            if (packet.motionId == null) {
                patchInterface.queIdleAction(null);
                return;
            }

            patchInterface.getIdleActionAnimations().stream()
                    .filter(pair -> pair.getSecond().get().getLocation().equals(packet.motionId))
                    .findFirst()
                    .ifPresent(accessor -> {
                        patchInterface.queIdleAction(accessor.getSecond());
                        patch.getAnimator().playAnimation(accessor.getSecond(), accessor.getSecond().get().getTransitionTime());
                    });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}