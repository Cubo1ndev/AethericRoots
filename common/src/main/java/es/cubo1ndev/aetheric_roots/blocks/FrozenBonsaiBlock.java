package es.cubo1ndev.aetheric_roots.blocks;

import com.mojang.serialization.MapCodec;

import es.cubo1ndev.aetheric_roots.bonsai.tree.type.BonsaiTreeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FrozenBonsaiBlock extends Block {
    public static final MapCodec<FrozenBonsaiBlock> CODEC = simpleCodec(FrozenBonsaiBlock::new);

    public static final IntegerProperty TREE_TYPE = IntegerProperty.create("tree_type", 1, BonsaiTreeType.COUNT);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape NS_SHAPE = Block.box(4, 0, 2, 12, 14, 14);
    private static final VoxelShape EW_SHAPE = Block.box(2, 0, 4, 14, 14, 12);

    public FrozenBonsaiBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TREE_TYPE, 1));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, TREE_TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos,
            CollisionContext collisionContext) {
        Direction direction = blockState.getValue(FACING);
        return direction.getAxis() == Axis.X ? NS_SHAPE : EW_SHAPE;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextInt(3) == 0) {
            double x = blockPos.getX() + 0.2 + randomSource.nextDouble() * 0.6;
            double y = blockPos.getY() + 0.5 + randomSource.nextDouble() * 0.5;
            double z = blockPos.getZ() + 0.2 + randomSource.nextDouble() * 0.6;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, -0.04, 0.0);
        }
    }
}
