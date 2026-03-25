package es.cubo1ndev.aetheric_roots.fabric.client;

import es.cubo1ndev.aetheric_roots.ExampleMod;
import es.cubo1ndev.aetheric_roots.blocks.BonsaiBlock;
import es.cubo1ndev.aetheric_roots.blocks.BonsaiRenderer;
import es.cubo1ndev.aetheric_roots.blocks.FrozenBonsaiBlock;
import es.cubo1ndev.aetheric_roots.client.ModItemProperties;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class ExampleModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ExampleMod.BONSAI_BLOCK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ExampleMod.FROZEN_BONSAI_BLOCK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ExampleMod.THORNED_VINE_BLOCK.get(), RenderType.cutout());

        BlockEntityRenderers.register(ExampleMod.BONSAI_BLOCK_ENTITY.get(), BonsaiRenderer::new);

        ColorProviderRegistry.BLOCK.register((state, blockAndTintGetter, pos, tintIndex) ->
                getFoliageColor(state, BonsaiBlock.TREE_TYPE, blockAndTintGetter, pos, tintIndex),
                ExampleMod.BONSAI_BLOCK.get());

        ColorProviderRegistry.BLOCK.register((state, blockAndTintGetter, pos, tintIndex) ->
                getFoliageColor(state, FrozenBonsaiBlock.TREE_TYPE, blockAndTintGetter, pos, tintIndex),
                ExampleMod.FROZEN_BONSAI_BLOCK.get());

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                    if (tintIndex != 0) return -1;
                    BlockItemStateProperties props = stack.getOrDefault(
                            DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
                    BlockState state = props.apply(
                            ExampleMod.BONSAI_BLOCK.get().defaultBlockState());
                    return getItemFoliageColor(state.getValue(BonsaiBlock.TREE_TYPE));
                },
                ExampleMod.BONSAI_ITEM.get());

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                    if (tintIndex != 0) return -1;
                    BlockItemStateProperties props = stack.getOrDefault(
                            DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
                    BlockState state = props.apply(
                            ExampleMod.FROZEN_BONSAI_BLOCK.get().defaultBlockState());
                    return getItemFoliageColor(state.getValue(FrozenBonsaiBlock.TREE_TYPE));
                },
                ExampleMod.FROZEN_BONSAI_ITEM.get());

        ColorProviderRegistry.BLOCK.register((state, blockAndTintGetter, pos, tintIndex) -> {
                    if (tintIndex != 0) return -1;
                    if (blockAndTintGetter != null && pos != null) {
                        return BiomeColors.getAverageFoliageColor(blockAndTintGetter, pos);
                    }
                    return FoliageColor.getDefaultColor();
                },
                ExampleMod.THORNED_VINE_BLOCK.get());

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                    if (tintIndex != 0) return -1;
                    return FoliageColor.getDefaultColor();
                },
                ExampleMod.THORNED_VINE_ITEM.get());

        ModItemProperties.register();
    }

    private static int getItemFoliageColor(int treeType) {
        return switch (treeType) {
            case 2 -> FoliageColor.getEvergreenColor();   // spruce
            case 3 -> FoliageColor.getBirchColor();       // birch
            case 7 -> -1;                                 // cherry (no tint)
            case 1, 4, 5, 6, 8 -> FoliageColor.getDefaultColor(); // oak, jungle, acacia, dark_oak, mangrove
            default -> -1;
        };
    }

    private static int getFoliageColor(BlockState state, IntegerProperty treeTypeProp,
            net.minecraft.world.level.BlockAndTintGetter blockAndTintGetter,
            net.minecraft.core.BlockPos pos, int tintIndex) {
        if (tintIndex != 0) return -1;
        int treeType = state.getValue(treeTypeProp);
        return switch (treeType) {
            case 2 -> FoliageColor.getEvergreenColor();   // spruce
            case 3 -> FoliageColor.getBirchColor();       // birch
            case 7 -> -1;                                 // cherry (no tint)
            case 1, 4, 5, 6, 8 -> {                      // oak, jungle, acacia, dark_oak, mangrove
                if (blockAndTintGetter != null && pos != null) {
                    yield BiomeColors.getAverageFoliageColor(blockAndTintGetter, pos);
                }
                yield FoliageColor.getDefaultColor();
            }
            default -> -1;
        };
    }
}
