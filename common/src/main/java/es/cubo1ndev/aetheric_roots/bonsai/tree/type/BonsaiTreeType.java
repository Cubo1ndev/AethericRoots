package es.cubo1ndev.aetheric_roots.bonsai.tree.type;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import es.cubo1ndev.aetheric_roots.ExampleMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class BonsaiTreeType {
    private static final Map<Item, BonsaiTreeType> BY_SAPLING = new HashMap<>();
    private static final Map<Integer, BonsaiTreeType> BY_ID = new HashMap<>();

    public static final int COUNT;

    public static final BonsaiTreeType OAK = register(1, Items.OAK_SAPLING, BonsaiTreeBehaviors.CROP, "crop");
    public static final BonsaiTreeType SPRUCE = register(2, Items.SPRUCE_SAPLING, BonsaiTreeBehaviors.MAGNET, "magnet"); // Item magnet. Candles = more radius.
    public static final BonsaiTreeType BIRCH = register(3, Items.BIRCH_SAPLING, BonsaiTreeBehaviors.PURIFICATOR, "purificator"); // Purificator, removes bad effects. Consumes candle. Candles = more radius + stronger bad effects.
    public static final BonsaiTreeType JUNGLE = register(4, Items.JUNGLE_SAPLING, BonsaiTreeBehaviors.THORN_AURA, "thorn_aura"); // Thorn Aura, spawns vines that expand. Players get damaged and slowed. Candles = more radius.
    public static final BonsaiTreeType ACACIA = register(5, Items.ACACIA_SAPLING, BonsaiTreeBehaviors.KNOCKBACK, "knockback"); // Knockback. Consumes candle. Candles = stronger knowback.
    public static final BonsaiTreeType DARK_OAK = register(6, Items.DARK_OAK_SAPLING, BonsaiTreeBehaviors.DARKNESS_XP, "darkness_xp"); // XP on dark areas. Candles = more xp.
    public static final BonsaiTreeType CHERRY = register(7, Items.CHERRY_SAPLING, BonsaiTreeBehaviors.PROTECTION, "protection");
    public static final BonsaiTreeType MANGROVE = register(8, Items.MANGROVE_PROPAGULE, BonsaiTreeBehaviors.SLOWER_BONSAI_GROW, "slower_bonsai_grow"); // Bonsai growing slows down. Candles = slower.
    //public static final BonsaiTreeType CRIMSON = register(9, Items.CRIMSON_FUNGUS, BonsaiTreeBehaviors.EMPTY); // Gives strength. Candles = stronger effect.
    //public static final BonsaiTreeType WARPED = register(10, Items.WARPED_FUNGUS, BonsaiTreeBehaviors.EMPTY); // Teleports player when receiving damage to a "safe" zone. Consumes candle. Candle = faster teleport.
    //public static final BonsaiTreeType PALE_OAK = register(11, Items.PALE_OAK_SAPLING, BonsaiTreeBehaviors.EMPTY); // No spawning zone.

    private final int id;
    private final Item sapling;
    private final BonsaiTreeBehavior behavior;
    private final String translationId;

    private BonsaiTreeType(int id, Item sapling, BonsaiTreeBehavior behavior, String translationId) {
        this.id = id;
        this.sapling = sapling;
        this.behavior = behavior;
        this.translationId = ExampleMod.MOD_ID + ":" + translationId;
    }

    private static BonsaiTreeType register(int id, Item sapling, BonsaiTreeBehavior behavior, String translationId) {
        BonsaiTreeType type = new BonsaiTreeType(id, sapling, behavior, translationId);
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

    public String getTranslationId() {
        return translationId;
    }

    static {
        COUNT = BY_ID.size();
    }
}
