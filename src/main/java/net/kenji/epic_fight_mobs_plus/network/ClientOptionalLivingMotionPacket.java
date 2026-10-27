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
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.function.Supplier;

public record ClientOptionalLivingMotionPacket(int entityId, int motionId) implements CustomPacketPayload{

    public static final CustomPacketPayload.Type<ClientOptionalLivingMotionPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EpicFightMobsPlus.MODID, "client_optional_living_motions_packet"));
    public static final StreamCodec<FriendlyByteBuf, ClientOptionalLivingMotionPacket> STREAM_CODEC =
            StreamCodec.of(ClientOptionalLivingMotionPacket::encode, ClientOptionalLivingMotionPacket::decode);


    // Encode: Write data to buffer
    public static void encode(FriendlyByteBuf buf, ClientOptionalLivingMotionPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeInt(packet.motionId);

    }

    // Decode: Read data from buffer
    public static ClientOptionalLivingMotionPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        int motionId = buf.readInt();
        return new ClientOptionalLivingMotionPacket(entityId, motionId);
    }

    // Handle: Process the packet on the receiving side
    public static void handle(ClientOptionalLivingMotionPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                executeOnClient(packet);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private static void executeOnClient(ClientOptionalLivingMotionPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        PlayerPatch<?> playerPatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        if (playerPatch == null) return;
        Entity entity = player.level().getEntity(packet.entityId);
        if (entity == null) return;
        EntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, EntityPatch.class);
        if (entityPatch == null) return;
        if (entityPatch instanceof IAnimalMobPatch patchInterface) {
            LivingEntityPatch<?> patch = patchInterface.getEntityPatch();
            if (patch != null) {
                patchInterface.setOptionalLivingMotion(packet.motionId);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}