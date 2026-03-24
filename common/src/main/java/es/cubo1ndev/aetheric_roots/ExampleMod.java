package es.cubo1ndev.aetheric_roots;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import es.cubo1ndev.aetheric_roots.blocks.BonsaiBlock;
import es.cubo1ndev.aetheric_roots.blocks.BonsaiBlockEntity;
import es.cubo1ndev.aetheric_roots.blocks.EmptyBonsaiBlock;
import es.cubo1ndev.aetheric_roots.blocks.FrozenBonsaiBlock;
import es.cubo1ndev.aetheric_roots.bonsai.tree.type.BonsaiTreeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ExampleMod {
        public static final String MOD_ID = "aetheric_roots";
        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID,
                        Registries.BLOCK);
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registries.ITEM);
        public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(MOD_ID,
                        Registries.CREATIVE_MODE_TAB);
        public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(MOD_ID,
                        Registries.BLOCK_ENTITY_TYPE);

        public static final RegistrySupplier<Block> EMPTY_BONSAI_BLOCK = BLOCKS.register("empty_bonsai",
                        () -> new EmptyBonsaiBlock(BlockBehaviour.Properties.of()
                                        .strength(0.5f)
                                        .sound(SoundType.STONE).noOcclusion()));

        public static final RegistrySupplier<Block> BONSAI_BLOCK = BLOCKS.register("bonsai",
                        () -> new BonsaiBlock(BlockBehaviour.Properties.of()
                                        .strength(0.5f).randomTicks()
                                        .sound(SoundType.STONE).noOcclusion()
                                        .lightLevel(blockState -> {
                                                int candleLight = blockState.getValue(BonsaiBlock.CANDLES) > 0 ? 12 : 0;
                                                int growthLight = switch (blockState
                                                                .getValue(BonsaiBlock.GROWTH_STATE)) {
                                                        case 1, 2 -> 0;
                                                        case 3 -> 10;
                                                        case 4 -> 15;
                                                        case 5 -> 5;
                                                        default -> 0;
                                                };
                                                return Math.max(candleLight, growthLight);
                                        })));

        public static final RegistrySupplier<Block> FROZEN_BONSAI_BLOCK = BLOCKS.register("frozen_bonsai",
                        () -> new FrozenBonsaiBlock(BlockBehaviour.Properties.of()
                                        .strength(0.5f)
                                        .sound(SoundType.GLASS).noOcclusion()));

        public static final RegistrySupplier<Block> ETHER_LEAVES_BLOCK = BLOCKS.register("ether_leaves",
                        () -> new LeavesBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.PLANT).strength(0.2F)
                                        .randomTicks().sound(SoundType.GRASS)
                                        .noOcclusion().isSuffocating((_1, _2, _3) -> false)
                                        .isViewBlocking((_1, _2, _3) -> false).ignitedByLava()
                                        .pushReaction(PushReaction.DESTROY)
                                        .isRedstoneConductor((_1, _2, _3) -> false)));

        public static final RegistrySupplier<BlockEntityType<BonsaiBlockEntity>> BONSAI_BLOCK_ENTITY = BLOCK_ENTITIES
                        .register("bonsai", () -> BlockEntityType.Builder
                                        .of(BonsaiBlockEntity::new, BONSAI_BLOCK.get()).build(null));

        public static final RegistrySupplier<Item> EMPTY_BONSAI_ITEM = ITEMS.register("empty_bonsai",
                        () -> new BlockItem(EMPTY_BONSAI_BLOCK.get(), new Item.Properties()));

        public static final RegistrySupplier<Item> BONSAI_ITEM = ITEMS.register("bonsai",
                        () -> new BlockItem(BONSAI_BLOCK.get(), new Item.Properties()));

        public static final RegistrySupplier<Item> FROZEN_BONSAI_ITEM = ITEMS.register("frozen_bonsai",
                        () -> new BlockItem(FROZEN_BONSAI_BLOCK.get(), new Item.Properties()));

        public static final RegistrySupplier<Item> ETHER_LEAVES_ITEM = ITEMS.register("ether_leaves",
                        () -> new BlockItem(ETHER_LEAVES_BLOCK.get(), new Item.Properties()));

        public static final RegistrySupplier<Item> ETHER_CANDLE_ITEM = ITEMS.register("ether_candle",
                        () -> new Item(new Item.Properties()));

        public static final RegistrySupplier<Item> ETHER_SAP_ITEM = ITEMS.register("ether_sap",
                        () -> new Item(new Item.Properties()));

        public static final RegistrySupplier<Item> ETHER_WAX_BALL_ITEM = ITEMS.register("ether_wax_ball",
                        () -> new Item(new Item.Properties()));

        public static final RegistrySupplier<CreativeModeTab> AETHERIC_TAB = TABS.register("aetheric_roots_tab",
                        () -> CreativeTabRegistry.create(builder -> {
                                builder.title(Component.translatable("itemGroup." + MOD_ID));
                                builder.icon(() -> new ItemStack(ETHER_CANDLE_ITEM.get()));
                                builder.displayItems((parameters, output) -> {
                                        output.accept(EMPTY_BONSAI_ITEM.get());
                                        for (int i = 1; i <= BonsaiTreeType.COUNT; i++) {
                                                BlockState state = FROZEN_BONSAI_BLOCK.get().defaultBlockState()
                                                                .setValue(FrozenBonsaiBlock.TREE_TYPE, i);
                                                ItemStack stack = new ItemStack(FROZEN_BONSAI_ITEM.get());
                                                stack.set(DataComponents.BLOCK_STATE,
                                                                BlockItemStateProperties.EMPTY
                                                                                .with(FrozenBonsaiBlock.TREE_TYPE,
                                                                                                state));
                                                output.accept(stack);
                                        }
                                        output.accept(ETHER_LEAVES_ITEM.get());
                                        output.accept(ETHER_SAP_ITEM.get());
                                        output.accept(ETHER_WAX_BALL_ITEM.get());
                                        output.accept(ETHER_CANDLE_ITEM.get());
                                });

                        }));

        public static void init() {
                BLOCKS.register();
                ITEMS.register();
                BLOCK_ENTITIES.register();
                TABS.register();
        }
}
