package com.enderio.base.client.renderer.travel;

import com.enderio.api.travel.ITravelTarget;
import com.enderio.api.travel.TravelRegistry;
import com.enderio.base.common.handler.TeleportHandler;
import com.enderio.base.common.travel.TravelSavedData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class RenderTravelTargets {

    @SubscribeEvent
    public static void renderLevel(RenderLevelStageEvent event) {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS)
            return;
        if (!TeleportHandler.canTeleport(player))
            return;
        boolean itemTeleport = TeleportHandler.canItemTeleport(player);
        TravelSavedData data = TravelSavedData.getTravelData(Minecraft.getInstance().level);
        Optional<ITravelTarget> activeTarget = TeleportHandler.getAnchorTarget(player);
        for (ITravelTarget target : data.getTravelTargets()) {
            double range = itemTeleport ? target.getItem2BlockRange() : target.getBlock2BlockRange();
            double distanceSquared = target.getPos().distToCenterSqr(player.position());
            if (range * range < distanceSquared || distanceSquared < 25)
                continue;
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            Camera mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 projectedView = mainCamera.getPosition();
            poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

            boolean active = activeTarget.map(act -> act == target).orElse(false);
            TravelRegistry.getRenderer(target).render(target, event.getLevelRenderer(), poseStack, distanceSquared, active);
            poseStack.popPose();
        }
    }
}
