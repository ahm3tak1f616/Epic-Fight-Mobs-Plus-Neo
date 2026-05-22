package net.kenji.epic_fight_mobs_plus.network;

import net.kenji.epic_fight_mobs_plus.EpicFightMobsPlus;
import net.kenji.epic_fight_mobs_plus.api.IdleActionManager;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleActionAnimation;
import net.kenji.epic_fight_mobs_plus.api.interfaces.IAnimalMobPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.neoforged.neoforge.network.handling.IPayloadContext;


import java.util.UUID;

public record ServerIdleActionPacket(
        UUID entityUuid,
        ResourceLocation motionId,   // null when isMotionNull
        float currentElapsedTime,
        float currentQuedTotalTime
) implements CustomPacketPayload {

    // Replace with your actual mod id path
    public static final CustomPacketPayload.Type<ServerIdleActionPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EpicFightMobsPlus.MODID, "server_idle_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerIdleActionPacket> STREAM_CODEC =
            StreamCodec.of(ServerIdleActionPacket::encode, ServerIdleActionPacket::decode);

    private static void encode(RegistryFriendlyByteBuf buf, ServerIdleActionPacket packet) {
        buf.writeUUID(packet.entityUuid);
        boolean isMotionNull = packet.motionId == null;
        buf.writeBoolean(isMotionNull);
        if (!isMotionNull)
            buf.writeResourceLocation(packet.motionId);
        buf.writeFloat(packet.currentElapsedTime);
        buf.writeFloat(packet.currentQuedTotalTime);
    }

    private static ServerIdleActionPacket decode(RegistryFriendlyByteBuf buf) {
        UUID entityUuid = buf.readUUID();
        boolean isMotionNull = buf.readBoolean();
        ResourceLocation motionId = null;
        if (!isMotionNull)
            motionId = buf.readResourceLocation();
        float elapsedTime = buf.readFloat();
        float quedTotalTime = buf.readFloat();
        return new ServerIdleActionPacket(entityUuid, motionId, elapsedTime, quedTotalTime);
    }

    public static void handle(ServerIdleActionPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();

            if (player == null) return;
            if (player.level() instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(packet.entityUuid);
                if (entity == null) return;

                IdleActionManager.IdleActionState state = IdleActionManager.getIdleActionState(entity.getUUID());
                state.currentElapsedTime = packet.currentElapsedTime;
                state.currentQuedTotalTime = packet.currentQuedTotalTime;

                if (packet.motionId == null) return;

                // NeoForge 1.21.1 + Epic Fight: use getData attachment, not getCapability
                EntityPatch<?> entityPatch = EpicFightCapabilities.ENTITY_PATCH_PROVIDER.getCapability(entity);
                if (entityPatch == null) return;
                if(entityPatch instanceof IAnimalMobPatch patchInterface) {
                    if (patchInterface.getEntityPatch() != null) {
                        AnimationManager.AnimationAccessor<?> accessor = AnimationManager.byKey(packet.motionId);
                        if (accessor != null && accessor.get() instanceof IdleActionAnimation idleActionAnimation) {
                            //IdleActionManager.playQuedIdleAction(patchInterface, idleActionAnimation);
                        }
                    }
                }
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}