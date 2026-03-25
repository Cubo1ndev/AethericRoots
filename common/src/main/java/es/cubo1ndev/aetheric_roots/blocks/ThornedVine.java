package es.cubo1ndev.aetheric_roots.blocks;

import java.util.function.ToIntFunction;

import com.mojang.serialization.MapCodec;

import es.cubo1ndev.aetheric_roots.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ThornedVine extends MultifaceBlock implements BonemealableBlock {
   public static final MapCodec<ThornedVine> CODEC = simpleCodec(ThornedVine::new);
   private final MultifaceSpreader spreader = new MultifaceSpreader(this);

   public MapCodec<ThornedVine> codec() {
      return CODEC;
   }

   public ThornedVine(BlockBehaviour.Properties properties) {
      super(properties);
   }

   public static ToIntFunction<BlockState> emission(int i) {
      return (blockState) -> MultifaceBlock.hasAnyFace(blockState) ? i : 0;
   }

   protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
      return !blockPlaceContext.getItemInHand().is(ExampleMod.THORNED_VINE_ITEM) || super.canBeReplaced(blockState, blockPlaceContext);
   }

   public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
      return Direction.stream().anyMatch((direction) -> this.spreader.canSpreadInAnyDirection(blockState, levelReader, blockPos, direction.getOpposite()));
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
      return true;
   }

   public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
      this.spreader.spreadFromRandomFaceTowardRandomDirection(blockState, serverLevel, blockPos, randomSource);
   }

   protected boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
      return blockState.getFluidState().isEmpty();
   }

   public MultifaceSpreader getSpreader() {
      return this.spreader;
   }


   protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
      if (entity instanceof LivingEntity) {
         entity.makeStuckInBlock(blockState, new Vec3((double)0.5F, (double)0.5F, (double)0.5F));
         if (!level.isClientSide && (entity.xOld != entity.getX() || entity.zOld != entity.getZ())) {
            double d = Math.abs(entity.getX() - entity.xOld);
            double e = Math.abs(entity.getZ() - entity.zOld);
            if (d >= (double)0.003F || e >= (double)0.003F) {
               entity.hurt(ExampleMod.thornedVineDamage(level), 1.0F);
            }
         }

      }
   }
}
