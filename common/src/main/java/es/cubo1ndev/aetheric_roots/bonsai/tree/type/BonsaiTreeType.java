package es.cubo1ndev.aetheric_roots.bonsai.tree.type;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class BonsaiTreeType {
    private static final Map<Item, BonsaiTreeType> BY_SAPLING = new HashMap<>();
    private static final Map<Integer, BonsaiTreeType> BY_ID = new HashMap<>();

    public static final int COUNT = 8;

    public static final BonsaiTreeType OAK = register(1, Items.OAK_SAPLING, BonsaiTreeBehaviors.CROP);
    public static final BonsaiTreeType SPRUCE = register(2, Items.SPRUCE_SAPLING, BonsaiTreeBehaviors.EMPTY);
    public static final BonsaiTreeType BIRCH = register(3, Items.BIRCH_SAPLING, BonsaiTreeBehaviors.EMPTY);
    public static final BonsaiTreeType JUNGLE = register(4, Items.JUNGLE_SAPLING, BonsaiTreeBehaviors.EMPTY);
    public static final BonsaiTreeType ACACIA = register(5, Items.ACACIA_SAPLING, BonsaiTreeBehaviors.EMPTY);
    public static final BonsaiTreeType DARK_OAK = register(6, Items.DARK_OAK_SAPLING, BonsaiTreeBehaviors.EMPTY);
    public static final BonsaiTreeType CHERRY = register(7, Items.CHERRY_SAPLING, BonsaiTreeBehaviors.PROTECTION);
    public static final BonsaiTreeType MANGROVE = register(8, Items.MANGROVE_PROPAGULE, BonsaiTreeBehaviors.EMPTY);

    private final int id;
    private final Item sapling;
    private final BonsaiTreeBehavior behavior;

    private BonsaiTreeType(int id, Item sapling, BonsaiTreeBehavior behavior) {
        this.id = id;
        this.sapling = sapling;
        this.behavior = behavior;
    }

    private static BonsaiTreeType register(int id, Item sapling, BonsaiTreeBehavior behavior) {
        BonsaiTreeType type = new BonsaiTreeType(id, sapling, behavior);
        BY_SAPLING.put(sapling, type);
        BY_ID.put(id, type);
        return type;
    }

    @Nullable
    public static BonsaiTreeType getBySapling(Item sapling) {
        return BY_SAPLING.get(sapling);
    }

    @Nullable
    public static BonsaiTreeType getById(int id) {
        return BY_ID.get(id);
    }

    public int getId() {
        return id;
    }

    public Item getSapling() {
        return sapling;
    }

    public BonsaiTreeBehavior getBehavior() {
        return behavior;
    }
}
