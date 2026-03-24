package es.cubo1ndev.aetheric_roots.neoforge.client;

import es.cubo1ndev.aetheric_roots.ExampleMod;
import es.cubo1ndev.aetheric_roots.blocks.BonsaiBlock;
import es.cubo1ndev.aetheric_roots.blocks.FrozenBonsaiBlock;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ExampleModNeoForgeClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ExampleMod.FROZEN_BONSAI_ITEM.get(),
                    ResourceLocation.fromNamespaceAndPath(ExampleMod.MOD_ID, "tree_type"),
                    (stack, level, entity, seed) -> {
                        BlockItemStateProperties props = stack.getOrDefault(
                                DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
                        BlockState state = props.apply(
                                ExampleMod.FROZEN_BONSAI_BLOCK.get().defaultBlockState());
                        return state.getValue(FrozenBonsaiBlock.TREE_TYPE);
                    });
        });
    }

    @SubscribeEvent
    public static void onRegisterBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, blockAndTintGetter, pos, tintIndex) ->
                        getFoliageColor(state, BonsaiBlock.TREE_TYPE, blockAndTintGetter, pos, tintIndex),
                ExampleMod.BONSAI_BLOCK.get());

        event.register((state, blockAndTintGetter, pos, tintIndex) ->
                        getFoliageColor(state, FrozenBonsaiBlock.TREE_TYPE, blockAndTintGetter, pos, tintIndex),
                ExampleMod.FROZEN_BONSAI_BLOCK.get());
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
