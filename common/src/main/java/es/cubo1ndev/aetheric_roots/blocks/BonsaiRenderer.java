package es.cubo1ndev.aetheric_roots.blocks;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class BonsaiRenderer implements BlockEntityRenderer<BonsaiBlockEntity> {
    private static final BlockState CANDLE_STATE = Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true);
    private static final float RADIUS = 16.0F / 16.0F;

    private final BlockRenderDispatcher blockRenderer;

    public BonsaiRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(BonsaiBlockEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int candleCount = entity.getBlockState().getValue(BonsaiBlock.CANDLES);
        if (candleCount == 0) return;

        float multiplier = candleCount / 6f;
        float offset = entity.time * .03125f;
        for (int i = 0; i < candleCount; i++) {
            float angle = (float) (i * Math.PI * 2.0 / candleCount);

            double offsetX = Math.sin(angle + offset) * RADIUS * multiplier;
            double offsetZ = Math.cos(angle + offset) * RADIUS * multiplier;
            double offsetY = 0.75F + Math.sin(offset) * .25;
            
            poseStack.pushPose();
            poseStack.translate(offsetX, offsetY, offsetZ);
            this.blockRenderer.renderSingleBlock(CANDLE_STATE, poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();

            Level level = entity.getLevel();
            if (level != null) {
                BlockPos pos = entity.getBlockPos();
                double cx = pos.getX() + offsetX + 0.50;
                double cy = pos.getY() + offsetY + 0.44 + 0.06;
                double cz = pos.getZ() + offsetZ + 0.50;
                level.addParticle(ParticleTypes.SMALL_FLAME, cx, cy, cz, 0.0, 0.0, 0.0);
            }
        }
    }
}
