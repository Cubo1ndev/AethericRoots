package es.cubo1ndev.aetheric_roots.bonsai.tree.type;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import es.cubo1ndev.aetheric_roots.blocks.BonsaiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

public final class BonsaiTreeBehaviors {
    public static final BonsaiTreeBehavior EMPTY = (level, blockPos, blockState, entity) -> {};

    public static final BonsaiTreeBehavior CROP = (level, blockPos, blockState, entity) -> {
        if (level.isClientSide)
            return;

        if (blockState.getValue(BonsaiBlock.GROWTH_STATE) < 3)
            return;

        int candleCount = blockState.getValue(BonsaiBlock.CANDLES);
        if (candleCount > 0 && level.random.nextInt(12 / candleCount) == 0) {
            int multiplier = candleCount + 1;
            int offset = 2 * multiplier;
            BlockPos randomCropPos = blockPos.offset(
                    level.random.nextInt(-offset, offset), 0, level.random.nextInt(-offset, offset));
            BlockState cropState = level.getBlockState(randomCropPos);

            if (cropState.getBlock() instanceof BonemealableBlock growable) {
                growable.performBonemeal((ServerLevel) level, level.random, randomCropPos, cropState);

                ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        randomCropPos.getX() + 0.5, randomCropPos.getY() + 0.5, randomCropPos.getZ() + 0.5,
                        5, 0.2, 0.2, 0.2, 0.05);
            }
        }
    };

    public static final BonsaiTreeBehavior PROTECTION = (level, blockPos, blockState, entity) -> {
        if (level.isClientSide)
            return;

        if (blockState.getValue(BonsaiBlock.GROWTH_STATE) < 3)
            return;

        int candleCount = blockState.getValue(BonsaiBlock.CANDLES);
        if (candleCount > 0 && level.random.nextInt(48 / candleCount) == 0) {
            int multiplier = candleCount + 1;

            for (int i = 0; i < multiplier; i++) {
                spawnLightning(level, blockPos, blockState, multiplier);
            }
        }
    };

    @Nullable
    private static Monster findLightningTargetAround(ServerLevel level, BlockPos blockPos, int multiplier) {
        BlockPos blockPos2 = level.getHeightmapPos(Types.MOTION_BLOCKING, blockPos);

        AABB aABB = AABB.encapsulatingFullBlocks(blockPos2, new BlockPos(blockPos2.atY(level.getMaxBuildHeight())))
                .inflate((double) 3.0F * multiplier);

        List<Monster> list = level.getEntitiesOfClass(Monster.class, aABB,
                (livingEntity) -> livingEntity != null && livingEntity.isAlive()
                        && level.canSeeSky(livingEntity.blockPosition()));

        if (list.isEmpty())
            return null;

        return list.get(level.random.nextInt(list.size()));
    }

    private static void spawnLightning(Level level, BlockPos blockPos, BlockState blockState, int multiplier) {
        Monster monster = findLightningTargetAround((ServerLevel) level, blockPos, multiplier);
        if (monster == null)
            return;

        LightningBolt lightningBolt = (LightningBolt) EntityType.LIGHTNING_BOLT.create(level);
        if (lightningBolt == null)
            return;

        lightningBolt.moveTo(Vec3.atBottomCenterOf(monster.blockPosition()));
        lightningBolt.setVisualOnly(false);
        ((ServerLevel) level).addFreshEntity(lightningBolt);

        BonsaiBlock.decreaseCandles(null, blockState, level, blockPos);
    }

    private BonsaiTreeBehaviors() {}
}
