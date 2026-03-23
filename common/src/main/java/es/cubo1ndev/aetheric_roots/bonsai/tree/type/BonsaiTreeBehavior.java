package es.cubo1ndev.aetheric_roots.bonsai.tree.type;

import es.cubo1ndev.aetheric_roots.blocks.BonsaiBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface BonsaiTreeBehavior {
    void tick(Level level, BlockPos pos, BlockState state, BonsaiBlockEntity entity);
}