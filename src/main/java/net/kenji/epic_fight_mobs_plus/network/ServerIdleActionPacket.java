package net.kenji.epic_fight_mobs_plus.network;

import net.kenji.epic_fight_mobs_plus.api.IdleActionManager;
import net.kenji.epic_fight_mobs_plus.api.animation_types.IdleActionAnimation;
import net.kenji.epic_fight_mobs_plus.api.interfaces.IAnimalMobPatch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import org.jline.utils.Log;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class ServerIdleActionPacket {
    private final UUID entityUuid;
    private final ResourceLocation motionId;
    private final float currentElapsedTime;
    private final float currentQuedTotalTime;

    private final boolean isMotionNull;

    // Constructor only needs UUID and boolean - playerPatch is looked up on server
    public ServerIdleActionPacket(UUID entityUuid, ResourceLocation motion, float currentElapsedTime, float currentQuedTotalTime) {
        this.entityUuid = entityUuid;
        this.motionId = motion;
        this.isMotionNull = motionId == null;
        this.currentElapsedTime = currentElapsedTime;
        this.currentQuedTotalTime = currentQuedTotalTime;
    }

    public static void encode(ServerIdleActionPacket packet, FriendlyByteBuf buf) {
        buf.writeUUID(packet.entityUuid);
        buf.writeBoolean(packet.isMotionNull);
        if(!packet.isMotionNull)
            buf.writeResourceLocation(packet.motionId);
        buf.writeFloat(packet.currentElapsedTime);
        buf.writeFloat(packet.currentQuedTotalTime);
    }

    public static ServerIdleActionPacket decode(FriendlyByteBuf buf) {
        UUID entityUuid = buf.readUUID();
        boolean isMotionNull = buf.readBoolean();
        ResourceLocation isAiming = null;
        if(!isMotionNull)
            isAiming = buf.readResourceLocation();
        float elapsedTime = buf.readFloat();
        float currentQuedTotalTime = buf.readFloat();
        return new ServerIdleActionPacket(entityUuid, isAiming, elapsedTime, currentQuedTotalTime); // Fixed - matches constructor
    }

    public static void handle(ServerIdleActionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();

            if(player == null)return;
            if (player.level() instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(packet.entityUuid);
                if (entity == null) return;
                IdleActionManager.IdleActionState state = IdleActionManager.getIdleActionState(entity.getUUID());
                state.currentElapsedTime = packet.currentElapsedTime;
                state.currentQuedTotalTime = packet.currentQuedTotalTime;
                if(packet.isMotionNull) return;
                entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent((cap) -> {
                    if (cap instanceof IAnimalMobPatch patchInterface) {
                        LivingEntityPatch<?> patch = patchInterface.getEntityPatch();
                        if (patch != null) {
                            AnimationManager.AnimationAccessor<?> accessor = AnimationManager.byKey(packet.motionId);
                            if(accessor != null && accessor.get() instanceof IdleActionAnimation idleActionAnimation){
                               // IdleActionManager.playQuedIdleAction(patchInterface, idleActionAnimation);
                            }
                        }

                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}