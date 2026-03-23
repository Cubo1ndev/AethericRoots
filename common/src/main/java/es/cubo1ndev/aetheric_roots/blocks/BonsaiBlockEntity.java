package es.cubo1ndev.aetheric_roots.blocks;

import es.cubo1ndev.aetheric_roots.ExampleMod;
import es.cubo1ndev.aetheric_roots.bonsai.tree.type.BonsaiTreeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BonsaiBlockEntity extends BlockEntity implements BlockEntityTicker<BonsaiBlockEntity> {
    public int time;

    public BonsaiBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(ExampleMod.BONSAI_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public BonsaiBlockEntity(BlockEntityType<?> entity, BlockPos blockPos, BlockState blockState) {
        super(entity, blockPos, blockState);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, BonsaiBlockEntity entity) {
        ++entity.time;

        int treeTypeId = blockState.getValue(BonsaiBlock.TREE_TYPE);
        if (treeTypeId == 0)
            return;

        BonsaiTreeType treeType = BonsaiTreeType.getById(treeTypeId);
        if (treeType != null && treeType.getBehavior() != null) {
            treeType.getBehavior().tick(level, blockPos, blockState, entity);
        }
    }
}
