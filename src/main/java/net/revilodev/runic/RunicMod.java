package net.revilodev.runic;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.revilodev.runic.block.ModBlocks;
import net.revilodev.runic.client.RunicClient;
import net.revilodev.runic.client.RunicClientModels;
import net.revilodev.runic.datagen.DataGenerators;
import net.revilodev.runic.effect.ModMobEffects;
import net.revilodev.runic.item.ModCreativeModeTabs;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.loot.ModLootModifiers;
import net.revilodev.runic.network.RunicNetwork;
import net.revilodev.runic.particle.ModParticles;
import net.revilodev.runic.recipe.ModRecipeSerializers;
import net.revilodev.runic.recipe.ModRecipeTypes;
import net.revilodev.runic.registry.ModDataComponents;
import net.revilodev.runic.screen.ModMenuTypes;
import org.slf4j.Logger;


@Mod(RunicMod.MOD_ID)
public class RunicMod {
    public static final String MOD_ID = "runic";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RunicMod(ModContainer modContainer, IEventBus modEventBus) {
        modEventBus.addListener(DataGenerators::gatherData);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(RunicNetwork::registerPayloads);

        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModParticles.register(modEventBus);
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, RunicConfig.SPEC);

        ModRecipeTypes.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);


        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(RunicClient::onRegisterScreens);
            modEventBus.addListener(RunicClientModels::onClientSetup);
        }

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.EXPANSION_INSCRIPTION.get());
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.ARTISANS_WORKBENCH.get());
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }
}
