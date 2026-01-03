package net.revilodev.runic.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.block.custom.ArtisansWorkbench;
import net.revilodev.runic.block.custom.EtchingTableBlock;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, RunicMod.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, RunicMod.MOD_ID);

    public static final DeferredHolder<Block, ArtisansWorkbench> ARTISANS_WORKBENCH =
            BLOCKS.register("artisans_workbench",
                    () -> new ArtisansWorkbench(BlockBehaviour.Properties.of()
                            .strength(5.0F)
                            .sound(SoundType.STONE)));

    public static final DeferredHolder<Block, EtchingTableBlock> ETCHING_TABLE =
            BLOCKS.register("etching_table",
                    () -> new EtchingTableBlock(BlockBehaviour.Properties.of()
                            .strength(5.0F)
                            .sound(SoundType.STONE)));

    public static final DeferredHolder<Item, BlockItem> ARTISANS_WORKBENCH_ITEM =
            ITEMS.register("artisans_workbench",
                    () -> new BlockItem(ARTISANS_WORKBENCH.get(), new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> ETCHING_TABLE_ITEM =
            ITEMS.register("etching_table",
                    () -> new BlockItem(ETCHING_TABLE.get(), new Item.Properties()));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
