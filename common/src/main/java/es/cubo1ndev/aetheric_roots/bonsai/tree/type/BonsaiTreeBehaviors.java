package es.cubo1ndev.aetheric_roots.bonsai.tree.type;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import es.cubo1ndev.aetheric_roots.ExampleMod;
import es.cubo1ndev.aetheric_roots.blocks.BonsaiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

public final class BonsaiTreeBehaviors {
    private static final List<List<Holder<MobEffect>>> MOB_EFFECTS_PURIFICATOR = new ArrayList<>();
    
    public static final BonsaiTreeBehavior SLOWER_BONSAI_GROW = (level, blockPos, blockState, entity) -> {
    };

    public static final BonsaiTreeBehavior CROP = (level, blockPos, blockState, entity) -> {
        if (level.isClientSide)
            return;

        if (blockState.getValue(BonsaiBlock.GROWTH_STATE) < 3)
            return;

        int candleCount = blockState.getValue(BonsaiBlock.CANDLES);
        if (candleCount == 0)
            return;

        if (level.random.nextInt(12 / candleCount) != 0)
            return;

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
    };

    public static final BonsaiTreeBehavior MAGNET = (level, blockPos, blockState, entity) -> {
        if (level.isClientSide)
            return;

        if (blockState.getValue(BonsaiBlock.GROWTH_STATE) < 3)
            return;

        int candleCount = blockState.getValue(BonsaiBlock.CANDLES);
        if (candleCount == 0)
            return;

        int multiplier = candleCount + 1;
        BlockPos blockPos2 = level.getHeightmapPos(Types.MOTION_BLOCKING, blockPos);
        AABB aABB = AABB.encapsulatingFullBlocks(blockPos2, new BlockPos(blockPos2.atY(level.getMaxBuildHeight())))
                .inflate((double) 3.0F * multiplier);

        Vec3 center = blockPos.getCenter();
        double minDist = 1.0;
        double maxDist = 3.0 * multiplier;
        List<ItemEntity> list = level.getEntitiesOfClass(ItemEntity.class, aABB,
                (itemEntity) -> {
                    if (itemEntity == null)
                        return false;
                    double dist = itemEntity.position().distanceTo(center);
                    return dist >= minDist && dist <= maxDist;
                });

        for (ItemEntity itemEntity : list) {
            Vec3 diff = center.subtract(itemEntity.position());
            double distance = diff.length();
            if (distance > 0) {
                Vec3 desired = diff.scale(0.3 / distance);
                Vec3 current = itemEntity.getDeltaMovement();
                itemEntity.setDeltaMovement(current.lerp(desired, 0.15));
            }
        }
    };

    public static final BonsaiTreeBehavior PURIFICATOR = (level, blockPos, blockState, entity) -> {
        if (level.isClientSide)
            return;

        if (blockState.getValue(BonsaiBlock.GROWTH_STATE) < 3)
            return;

        int candleCount = blockState.getValue(BonsaiBlock.CANDLES);
        if (candleCount == 0)
            return;

        int multiplier = candleCount + 1;
        BlockPos blockPos2 = level.getHeightmapPos(Types.MOTION_BLOCKING, blockPos);
        AABB aABB = AABB.encapsulatingFullBlocks(blockPos2, new BlockPos(blockPos2.atY(level.getMaxBuildHeight())))
                .inflate((double) 3.0F * multiplier);

        List<Player> list = level.getEntitiesOfClass(Player.class, aABB,
                (livingEntity) -> livingEntity != null && livingEntity.isAlive());

        boolean effectRemoved = false;
        for (Player player : list) {
            for (int i = candleCount; i > 0; i--) {
                for (Holder<MobEffect> effect : MOB_EFFECTS_PURIFICATOR.get(i - 1)) {
                    if (player.removeEffect(effect)) {
                        effectRemoved = true;
                    }
                }
            }
        }

        if (!effectRemoved)
            return;
        BonsaiBlock.decreaseCandles(null, blockState, level, blockPos);
    };

    public static final BonsaiTreeBehavior KNOCKBACK = (level, blockPos, blockState, entity) -> {
        if (level.isClientSide)
            return;

        if (blockState.getValue(BonsaiBlock.GROWTH_STATE) < 3)
            return;

        int candleCount = blockState.getValue(BonsaiBlock.CANDLES);
        if (candleCount == 0)
            return;

        if (level.random.nextInt(96 / candleCount) != 0)
            return;

        int multiplier = candleCount + 1;
        Monster monster = findLightningTargetAround((ServerLevel) level, blockPos, multiplier);
        if (monster == null)
            return;

        double dx = monster.getX() - (blockPos.getX() + 0.5);
        double dz = monster.getZ() - (blockPos.getZ() + 0.5);
        monster.knockback(multiplier, -dx, -dz);

        BonsaiBlock.decreaseCandles(null, blockState, level, blockPos);
    };

    public static final BonsaiTreeBehavior DARKNESS_XP = (level, blockPos, blockState, entity) -> {
        if (level.isClientSide)
            return;

        if (blockState.getValue(BonsaiBlock.GROWTH_STATE) < 3)
            return;

        int candleCount = blockState.getValue(BonsaiBlock.CANDLES);
        if (candleCount == 0)
            return;

        if (level.random.nextInt(960 / candleCount) != 0)
            return;

        /*
        1 candle => b < 1
        6 candles => b < 6
        */
        if (level.getBrightness(LightLayer.BLOCK, blockPos) < candleCount) 
            return;

        ExperienceOrb.award((ServerLevel) level, blockPos.getCenter(), candleCount);
    };

    public static final BonsaiTreeBehavior PROTECTION = (level, blockPos, blockState, entity) -> {
        if (level.isClientSide)
            return;

        if (blockState.getValue(BonsaiBlock.GROWTH_STATE) < 3)
            return;

        int candleCount = blockState.getValue(BonsaiBlock.CANDLES);
        if (candleCount == 0)
            return;

        if (level.random.nextInt(48 / candleCount) != 0)
            return;

        int multiplier = candleCount + 1;

        for (int i = 0; i < multiplier; i++) {
            spawnLightning(level, blockPos, blockState, multiplier);
        }
    };

    

    public static final BonsaiTreeBehavior THORN_AURA = (level, blockPos, blockState, entity) -> {
        if (level.isClientSide)
            return;

        if (blockState.getValue(BonsaiBlock.GROWTH_STATE) < 3)
            return;

        int candleCount = blockState.getValue(BonsaiBlock.CANDLES);
        if (candleCount == 0)
            return;

        if (level.random.nextInt(12 / candleCount) != 0)
            return;

        // Random walk from the bonsai to find a placement spot, sculk-style
        // More candles = longer search = further spread
        int maxSteps = 4 + candleCount * 3;
        BlockPos cursor = blockPos;

        for (int step = 0; step < maxSteps; step++) {
            Direction walkDir = Direction.getRandom(level.random);
            cursor = cursor.relative(walkDir);

            if (tryPlaceVine(level, cursor))
                return;
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

    private static boolean tryPlaceVine(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir())
            return false;

        BlockState vineDefault = ExampleMod.THORNED_VINE_BLOCK.get().defaultBlockState();
        for (Direction face : Direction.values()) {
            BlockPos neighbor = pos.relative(face);
            if (level.getBlockState(neighbor).isFaceSturdy(level, neighbor, face)) {
                BlockState placed = vineDefault.setValue(MultifaceBlock.getFaceProperty(face), true);
                level.setBlockAndUpdate(pos, placed);
                ((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        5, 0.2, 0.2, 0.2, 0.05);
                return true;
            }
        }
        return false;
    }

    static {
        /* 1 candle  */ MOB_EFFECTS_PURIFICATOR.add(List.of(MobEffects.HUNGER, MobEffects.POISON));
        /* 2 candles */ MOB_EFFECTS_PURIFICATOR.add(List.of(MobEffects.DARKNESS, MobEffects.BLINDNESS, MobEffects.CONFUSION));
        /* 3 candles */ MOB_EFFECTS_PURIFICATOR.add(List.of(MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN));
        /* 4 candles */ MOB_EFFECTS_PURIFICATOR.add(List.of(MobEffects.WITHER, MobEffects.DIG_SLOWDOWN));
        /* 5 candles */ MOB_EFFECTS_PURIFICATOR.add(List.of(MobEffects.LEVITATION));
        /* 6 candles */ MOB_EFFECTS_PURIFICATOR.add(List.of(MobEffects.BAD_OMEN, MobEffects.RAID_OMEN, MobEffects.TRIAL_OMEN));
    }
}
