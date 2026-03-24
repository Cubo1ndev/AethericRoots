package es.cubo1ndev.aetheric_roots.client;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import es.cubo1ndev.aetheric_roots.ExampleMod;
import es.cubo1ndev.aetheric_roots.blocks.BonsaiBlock;
import es.cubo1ndev.aetheric_roots.blocks.FrozenBonsaiBlock;
import es.cubo1ndev.aetheric_roots.bonsai.tree.type.BonsaiTreeType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class ModItemProperties {
    public static void register() {
        ResourceLocation treeTypePredicate = ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "tree_type");

        ItemPropertiesRegistry.register(ExampleMod.BONSAI_ITEM.get(), treeTypePredicate,
                (stack, level, entity, seed) -> {
                    BlockItemStateProperties props = stack.getOrDefault(
                            DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
                    BlockState state = props.apply(
                            ExampleMod.BONSAI_BLOCK.get().defaultBlockState());
                    return state.getValue(BonsaiBlock.TREE_TYPE) / (float) BonsaiTreeType.COUNT;
                });

        ItemPropertiesRegistry.register(ExampleMod.FROZEN_BONSAI_ITEM.get(), treeTypePredicate,
                (stack, level, entity, seed) -> {
                    BlockItemStateProperties props = stack.getOrDefault(
                            DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
                    BlockState state = props.apply(
                            ExampleMod.FROZEN_BONSAI_BLOCK.get().defaultBlockState());
                    return state.getValue(FrozenBonsaiBlock.TREE_TYPE) / (float) BonsaiTreeType.COUNT;
                });
    }
}
