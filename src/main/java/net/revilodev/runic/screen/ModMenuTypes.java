package net.revilodev.runic.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.screen.custom.ArtisansWorkbenchMenu;
import net.revilodev.runic.screen.custom.EtchingTableMenu;

public final class ModMenuTypes {
    private ModMenuTypes() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, RunicMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ArtisansWorkbenchMenu>> ARTISANS_WORKBENCH =
            MENUS.register("artisans_workbench",
                    () -> IMenuTypeExtension.create((id, inv, buf) ->
                            ArtisansWorkbenchMenu.client(id, inv, buf.readBlockPos())
                    )
            );

    public static final DeferredHolder<MenuType<?>, MenuType<EtchingTableMenu>> ETCHING_TABLE =
            MENUS.register("etching_table",
                    () -> IMenuTypeExtension.create((id, inv, buf) ->
                            EtchingTableMenu.client(id, inv, buf.readBlockPos())
                    )
            );

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
