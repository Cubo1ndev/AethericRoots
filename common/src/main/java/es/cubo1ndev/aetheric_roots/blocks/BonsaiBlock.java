package es.cubo1ndev.aetheric_roots.blocks;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import es.cubo1ndev.aetheric_roots.ExampleMod;
import es.cubo1ndev.aetheric_roots.bonsai.tree.type.BonsaiTreeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BonsaiBlock extends BaseEntityBlock implements WorldlyContainerHolder {
    public static final MapCodec<BonsaiBlock> CODEC = simpleCodec(BonsaiBlock::new);

    public static final IntegerProperty TREE_TYPE = IntegerProperty.create("tree_type", 1, BonsaiTreeType.COUNT);
    public static final IntegerProperty GROWTH_STATE = IntegerProperty.create("growth_state", 1, 5);
    public static final IntegerProperty CANDLES = IntegerProperty.create("candles", 0, 6);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final int REACH = 5;

    private static final VoxelShape NS_1_SHAPE = Block.box(4, 0, 2, 12, 10, 14);
    private static final VoxelShape EW_1_SHAPE = Block.box(2, 0, 4, 14, 10, 12);
    private static final VoxelShape NS_2_SHAPE = Block.box(4, 0, 2, 12, 14, 14);
    private static final VoxelShape EW_2_SHAPE = Block.box(2, 0, 4, 14, 14, 12);

    public BonsaiBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(GROWTH_STATE, 1)
                .setValue(FACING, Direction.NORTH)
                .setValue(CANDLES, 0)
                .setValue(TREE_TYPE, 1));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BonsaiBlockEntity(blockPos, blockState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return (Integer) blockState.getValue(GROWTH_STATE);
    }

    @Override
    public <U extends BlockEntity> BlockEntityTicker<U> getTicker(Level level, BlockState blockState,
            BlockEntityType<U> toCheckBlockEntityType) {
        return createTickerHelper(toCheckBlockEntityType, ExampleMod.BONSAI_BLOCK_ENTITY.get(),
                (lvl, pos, state, entity) -> entity.tick(lvl, pos, state, entity));
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        ItemStack stack = super.getCloneItemStack(levelReader, blockPos, blockState);
        stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(TREE_TYPE, blockState));
        stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(GROWTH_STATE, blockState));
        stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(CANDLES, blockState));
        return stack;
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    private void summonParticles(Level level, BlockPos blockPos, RandomSource randomSource, ParticleOptions particle) {
        double x = blockPos.getX() + 0.2 + randomSource.nextDouble() * 0.6;
        double y = blockPos.getY() + 0.5 + randomSource.nextDouble() * 0.5;
        double z = blockPos.getZ() + 0.2 + randomSource.nextDouble() * 0.6;

        level.addParticle(particle, x, y, z, 0.0, -0.04, 0.0);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level,
            BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        int currentGrowth = blockState.getValue(GROWTH_STATE);

        // Freeze with wax ball (only at growth 3+)
        if (itemStack.is(ExampleMod.ETHER_WAX_BALL_ITEM.get()) && currentGrowth >= 3) {
            if (!level.isClientSide) {
                // Drop candles
                int candles = blockState.getValue(CANDLES);
                if (candles > 0) {
                    Block.popResource(level, blockPos,
                            new ItemStack(ExampleMod.ETHER_CANDLE_ITEM.get(), candles));
                }
                // Replace with frozen bonsai
                BlockState frozenState = ExampleMod.FROZEN_BONSAI_BLOCK.get().defaultBlockState()
                        .setValue(FrozenBonsaiBlock.FACING, blockState.getValue(FACING))
                        .setValue(FrozenBonsaiBlock.TREE_TYPE, blockState.getValue(TREE_TYPE));
                level.setBlockAndUpdate(blockPos, frozenState);
                level.playSound(null, blockPos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Add candles
        if (itemStack.is(ExampleMod.ETHER_CANDLE_ITEM.get())) {
            int currentCandles = blockState.getValue(CANDLES);
            if (currentCandles >= 6) {
                return ItemInteractionResult.FAIL;
            }
            if (!level.isClientSide) {
                level.setBlockAndUpdate(blockPos, blockState.setValue(CANDLES, currentCandles + 1));
                level.playSound(null, blockPos, SoundEvents.CANDLE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Shears interaction
        if (!itemStack.is(Items.SHEARS)) {
            return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
        } else if (level.isClientSide) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Shears on growth state 1: unequip sapling, revert to empty bonsai
        if (currentGrowth == 1) {
            int treeTypeId = blockState.getValue(TREE_TYPE);
            BonsaiTreeType treeType = BonsaiTreeType.getById(treeTypeId);
            if (treeType != null) {
                Block.popResource(level, blockPos, new ItemStack(treeType.getSapling()));
            }
            BlockState emptyState = ExampleMod.EMPTY_BONSAI_BLOCK.get().defaultBlockState()
                    .setValue(EmptyBonsaiBlock.FACING, blockState.getValue(FACING));
            level.setBlockAndUpdate(blockPos, emptyState);
            level.playSound(null, blockPos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(interactionHand));
            level.gameEvent(player, GameEvent.SHEAR, blockPos);
            player.awardStat(Stats.ITEM_USED.get(Items.SHEARS));
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Shears on growth state 2+: prune
        int nextLevel = Math.max(1, currentGrowth - 1);
        level.setBlockAndUpdate(blockPos, blockState.setValue(GROWTH_STATE, nextLevel));
        level.playSound(null, blockPos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);

        for (int i = 0; i < 5; i++) {
            summonParticles(level, blockPos, level.getRandom(), ParticleTypes.CRIT);
        }

        if (currentGrowth >= 3) {
            List<ItemStack> items = getItems(itemStack, blockState, level, blockPos, player);
            for (ItemStack item : items) {
                Block.popResource(level, blockPos, item);
            }
        }

        itemStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(interactionHand));
        level.gameEvent(player, GameEvent.SHEAR, blockPos);
        player.awardStat(Stats.ITEM_USED.get(Items.SHEARS));
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private List<ItemStack> getItems(@Nullable ItemStack itemStack, BlockState blockState, LevelAccessor levelAccessor,
            BlockPos blockPos, @Nullable Player player) {
        LootParams.Builder builder = new LootParams.Builder((ServerLevel) levelAccessor)
                .withParameter(LootContextParams.ORIGIN, blockPos.getCenter())
                .withParameter(LootContextParams.BLOCK_STATE, blockState);

        if (itemStack != null) {
            builder.withParameter(LootContextParams.TOOL, itemStack);
        } else {
            builder.withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
        }

        if (player != null) {
            builder.withParameter(LootContextParams.THIS_ENTITY, player);
        }

        LootParams params = builder.create(LootContextParamSets.BLOCK);

        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "gameplay/bonsai_pruning"));
        LootTable lootTable = levelAccessor.getServer().reloadableRegistries().getLootTable(lootTableKey);

        return lootTable.getRandomItems(params);
    }

    @Override
    public WorldlyContainer getContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        int currentGrowth = blockState.getValue(GROWTH_STATE);
        int currentCandles = blockState.getValue(CANDLES);

        boolean canInput = currentCandles < 6;
        boolean canOutput = currentGrowth >= 4;

        if (!canInput && !canOutput)
            return new EmptyContainer();

        List<ItemStack> outputItems = canOutput
                ? getItems(null, blockState, levelAccessor, blockPos, null)
                : List.of();

        return new InputOutputContainer(blockState, levelAccessor, blockPos, canInput, outputItems);
    }
   
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(GROWTH_STATE, FACING, CANDLES, TREE_TYPE);
    }

    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) this.defaultBlockState().setValue(FACING,
                blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos,
            CollisionContext collisionContext) {
        Direction direction = (Direction) blockState.getValue(FACING);
        return switch (blockState.getValue(GROWTH_STATE)) {
            case 1:
                yield direction.getAxis() == Axis.X ? NS_1_SHAPE : EW_1_SHAPE;
            case 2:
            case 3:
            case 4:
            case 5:
                yield direction.getAxis() == Axis.X ? NS_2_SHAPE : EW_2_SHAPE;
            default:
                yield direction.getAxis() == Axis.X ? NS_1_SHAPE : EW_1_SHAPE;
        };
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos,
            RandomSource randomSource) {
        int currentGrowth = blockState.getValue(GROWTH_STATE);

        int candles = blockState.getValue(CANDLES) + 1;
        if ((currentGrowth <= 2 && randomSource.nextInt(10) == 0)
                || (currentGrowth > 2 && randomSource.nextInt(20 * candles) == 0)) {
            if (currentGrowth == 5) {
                serverLevel.explode(null, Explosion.getDefaultDamageSource(serverLevel, null), null, blockPos.getX(),
                        blockPos.getY() + 0.0625F, blockPos.getZ(), 4.0F, false, ExplosionInteraction.TRIGGER);
                return;
            }

            int nextLevel = Math.min(5, currentGrowth + 1);
            serverLevel.setBlock(blockPos, blockState.setValue(GROWTH_STATE, nextLevel), 3);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        int growth = blockState.getValue(GROWTH_STATE);
        if (growth < 4) {
            return;
        }

        if (randomSource.nextInt(3) == 0) {
            if (growth == 4) {
                summonParticles(level, blockPos, randomSource, ParticleTypes.END_ROD);
            } else {
                summonParticles(level, blockPos, randomSource, ParticleTypes.DRAGON_BREATH);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide && !player.getAbilities().instabuild) {
            int treeTypeId = blockState.getValue(TREE_TYPE);
            BonsaiTreeType treeType = BonsaiTreeType.getById(treeTypeId);
            if (treeType != null) {
                Block.popResource(level, blockPos, new ItemStack(treeType.getSapling()));
            }
        }
        return super.playerWillDestroy(level, blockPos, blockState, player);
    }

    public static BlockState decreaseGrowth(@Nullable Entity entity, BlockState blockState, LevelAccessor levelAccessor,
            BlockPos blockPos) {
        BlockState blockState2 = (BlockState) blockState.setValue(GROWTH_STATE, blockState.getValue(GROWTH_STATE) - 1);
        levelAccessor.setBlock(blockPos, blockState2, 3);
        levelAccessor.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, Context.of(entity, blockState2));
        return blockState2;
    }

    public static BlockState decreaseCandles(@Nullable Entity entity, BlockState blockState, LevelAccessor levelAccessor,
            BlockPos blockPos) {
        BlockState blockState2 = (BlockState) blockState.setValue(CANDLES, blockState.getValue(CANDLES) - 1);
        levelAccessor.setBlock(blockPos, blockState2, 3);
        levelAccessor.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, Context.of(entity, blockState2));
        return blockState2;
    }

    public static BlockState addCandles(@Nullable Entity entity, BlockState blockState, LevelAccessor levelAccessor,
            BlockPos blockPos) {
        BlockState blockState2 = (BlockState) blockState.setValue(CANDLES, blockState.getValue(CANDLES) + 1);
        levelAccessor.setBlock(blockPos, blockState2, 3);
        levelAccessor.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, Context.of(entity, blockState2));
        return blockState2;
    }

    static class EmptyContainer extends SimpleContainer implements WorldlyContainer {
        public EmptyContainer() {
            super(0);
        }

        public int[] getSlotsForFace(Direction direction) {
            return new int[0];
        }

        public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
            return false;
        }

        public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
            return false;
        }
    }

    static class InputOutputContainer extends SimpleContainer implements WorldlyContainer {
        private final BlockState state;
        private final LevelAccessor level;
        private final BlockPos pos;
        private final boolean canInput;
        private final int outputStart;
        private boolean inputChanged;
        private boolean outputChanged;

        public InputOutputContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos,
                boolean canInput, List<ItemStack> outputItems) {
            super(1 + outputItems.size());
            this.state = blockState;
            this.level = levelAccessor;
            this.pos = blockPos;
            this.canInput = canInput;
            this.outputStart = 1;
            for (int i = 0; i < outputItems.size(); i++) {
                this.setItem(1 + i, outputItems.get(i));
            }
        }

        public int getMaxStackSize() {
            return 64;
        }

        public int[] getSlotsForFace(Direction direction) {
            if (direction == Direction.UP && canInput) {
                return new int[] { 0 };
            }
            if (direction == Direction.DOWN) {
                int outputCount = this.getContainerSize() - outputStart;
                int[] slots = new int[outputCount];
                for (int i = 0; i < outputCount; i++)
                    slots[i] = outputStart + i;
                return slots;
            }
            return new int[0];
        }

        public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
            return !this.inputChanged && i == 0 && direction == Direction.UP
                    && itemStack.is(ExampleMod.ETHER_CANDLE_ITEM.get());
        }

        public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
            return !this.outputChanged && i >= outputStart && direction == Direction.DOWN;
        }

        public void setChanged() {
            ItemStack inputStack = this.getItem(0);
            if (!inputStack.isEmpty() && !this.inputChanged) {
                this.inputChanged = true;
                BlockState blockState = BonsaiBlock.addCandles((Entity) null, this.state, this.level, this.pos);
                this.level.levelEvent(1500, this.pos, blockState != this.state ? 1 : 0);
                this.removeItemNoUpdate(0);
            }

            for (int i = outputStart; i < this.getContainerSize(); i++) {
                if (this.getItem(i).isEmpty() && !this.outputChanged) {
                    this.outputChanged = true;
                    BonsaiBlock.decreaseGrowth((Entity) null, this.state, this.level, this.pos);
                    break;
                }
            }
        }
    }
}
