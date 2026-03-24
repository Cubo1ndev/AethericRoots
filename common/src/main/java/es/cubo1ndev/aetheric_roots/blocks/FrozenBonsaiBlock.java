package es.cubo1ndev.aetheric_roots.blocks;

import java.util.List;

import com.mojang.serialization.MapCodec;

import es.cubo1ndev.aetheric_roots.ExampleMod;
import es.cubo1ndev.aetheric_roots.bonsai.tree.type.BonsaiTreeType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
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
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level,
            BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {

        // Shears interaction
        if (!itemStack.is(Items.SHEARS)) {
            return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
        } else if (level.isClientSide) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Shears on freeze: place back the working bonsai
        BlockState unFrozenState = ExampleMod.BONSAI_BLOCK.get().defaultBlockState()
                .setValue(BonsaiBlock.FACING, blockState.getValue(FACING))
                .setValue(BonsaiBlock.TREE_TYPE, blockState.getValue(TREE_TYPE))
                .setValue(BonsaiBlock.GROWTH_STATE, 3);
        level.setBlockAndUpdate(blockPos, unFrozenState);
        level.playSound(null, blockPos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
        Block.popResource(level, blockPos, new ItemStack(ExampleMod.ETHER_WAX_BALL_ITEM.get(), 1));

        itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(interactionHand));
        level.gameEvent(player, GameEvent.SHEAR, blockPos);
        player.awardStat(Stats.ITEM_USED.get(Items.SHEARS));

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, TREE_TYPE);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        ItemStack stack = super.getCloneItemStack(levelReader, blockPos, blockState);
        stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(TREE_TYPE, blockState));
        return stack;
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
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0.0, -0.04, 0.0);
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list,
            TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, tooltipContext, list, tooltipFlag);

        int treeType = itemStack.get(DataComponents.BLOCK_STATE).get(TREE_TYPE);
        MutableComponent nameComponent = (MutableComponent) BonsaiTreeType.getById(treeType).getSapling().getName(itemStack);

        list.add(nameComponent.withStyle(ChatFormatting.YELLOW));
    }
}
