package net.revilodev.runic.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public class ModItemModelProvider extends ItemModelProvider {

    private static final ResourceLocation RUNE_MODEL_PRED =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "rune_model");

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, RunicMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.EXPANSION_INSCRIPTION.get());
        basicItem(ModItems.REPAIR_INSCRIPTION.get());
        basicItem(ModItems.UPGRADE_INSCRIPTION.get());
        basicItem(ModItems.NULLIFICATION_INSCRIPTION.get());
        basicItem(ModItems.REROLL_INSCRIPTION.get());
        basicItem(ModItems.WILD_INSCRIPTION.get());
        basicItem(ModItems.CURSED_INSCRIPTION.get());
        basicItem(ModItems.EXTRACTION_INSCRIPTION.get());

        generateLayeredRuneAndEtchingModels();
    }

    private void generateLayeredRuneAndEtchingModels() {
        generateLayeredSet(
                "enhanced_rune",
                "item/rune",
                "rune"
        );

        generateLayeredSet(
                "etching",
                "item/etching",
                "etching"
        );
    }

    private void generateLayeredSet(String itemModelName, String baseTexture, String folderName) {
        List<RuneModelDef> defs = runeModelDefs();

        for (RuneModelDef def : defs) {
            String modelPath = "item/" + folderName + "/" + def.subPath;
            String iconTex = "item/icons/" + def.subPath;

            withExistingParent(modelPath, "minecraft:item/generated")
                    .texture("layer0", modLoc(baseTexture))
                    .texture("layer1", modLoc(iconTex));
        }

        ItemModelBuilder base = withExistingParent("item/" + itemModelName, "minecraft:item/generated")
                .texture("layer0", modLoc(baseTexture));

        for (RuneModelDef def : defs) {
            String modelPath = "item/" + folderName + "/" + def.subPath;
            base.override()
                    .predicate(RUNE_MODEL_PRED, def.predicateValue)
                    .model(getExistingFile(modLoc(modelPath)))
                    .end();
        }
    }

    private static List<RuneModelDef> runeModelDefs() {
        List<RuneModelDef> out = new ArrayList<>();

        out.add(new RuneModelDef(1f, "stat/attack_damage"));
        out.add(new RuneModelDef(2f, "stat/attack_range"));
        out.add(new RuneModelDef(3f, "stat/attack_speed"));
        out.add(new RuneModelDef(4f, "stat/blast_resistance"));
        out.add(new RuneModelDef(5f, "stat/bleeding_chance"));
        out.add(new RuneModelDef(6f, "stat/bonus_chance"));
        out.add(new RuneModelDef(7f, "stat/draw_speed"));
        out.add(new RuneModelDef(8f, "stat/durability"));
        out.add(new RuneModelDef(9f, "stat/fall_reduction"));
        out.add(new RuneModelDef(10f, "stat/fire_resistance"));
        out.add(new RuneModelDef(11f, "stat/flame_chance"));
        out.add(new RuneModelDef(12f, "stat/freezing_chance"));
        out.add(new RuneModelDef(13f, "stat/health"));
        out.add(new RuneModelDef(14f, "stat/healing_efficiency"));
        out.add(new RuneModelDef(15f, "stat/jump_height"));
        out.add(new RuneModelDef(16f, "stat/knockback_resistance"));
        out.add(new RuneModelDef(17f, "stat/leeching_chance"));
        out.add(new RuneModelDef(18f, "stat/mining_speed"));
        out.add(new RuneModelDef(19f, "stat/movement_speed"));
        out.add(new RuneModelDef(20f, "stat/nether_damage"));
        out.add(new RuneModelDef(21f, "stat/poison_chance"));
        out.add(new RuneModelDef(22f, "stat/power"));
        out.add(new RuneModelDef(23f, "stat/projectile_resistance"));
        out.add(new RuneModelDef(24f, "stat/resistance"));
        out.add(new RuneModelDef(25f, "stat/shocking_chance"));
        out.add(new RuneModelDef(26f, "stat/stun_chance"));
        out.add(new RuneModelDef(27f, "stat/sweeping_range"));
        out.add(new RuneModelDef(28f, "stat/swimming_speed"));
        out.add(new RuneModelDef(29f, "stat/toughness"));
        out.add(new RuneModelDef(30f, "stat/undead_damage"));
        out.add(new RuneModelDef(31f, "stat/water_breathing"));
        out.add(new RuneModelDef(32f, "stat/weakening_chance"));
        out.add(new RuneModelDef(33f, "stat/withering_chance"));

        out.add(new RuneModelDef(34f, "effect/acrobat"));
        out.add(new RuneModelDef(35f, "effect/backstabbing"));
        out.add(new RuneModelDef(36f, "effect/binding_curse"));
        out.add(new RuneModelDef(37f, "effect/blocking"));
        out.add(new RuneModelDef(38f, "effect/breach"));
        out.add(new RuneModelDef(39f, "effect/capacity"));
        out.add(new RuneModelDef(40f, "effect/catalysis"));
        out.add(new RuneModelDef(41f, "effect/channeling"));
        out.add(new RuneModelDef(42f, "effect/chill_aura"));
        out.add(new RuneModelDef(43f, "effect/density"));
        out.add(new RuneModelDef(44f, "effect/destruction"));
        out.add(new RuneModelDef(45f, "effect/discharge"));
        out.add(new RuneModelDef(46f, "effect/ensnaring"));
        out.add(new RuneModelDef(47f, "effect/fire_react"));
        out.add(new RuneModelDef(48f, "effect/flame"));
        out.add(new RuneModelDef(49f, "effect/fortune"));
        out.add(new RuneModelDef(50f, "effect/frost_walker"));
        out.add(new RuneModelDef(51f, "effect/ground_slam"));
        out.add(new RuneModelDef(52f, "effect/impaling"));
        out.add(new RuneModelDef(53f, "effect/infinity"));
        out.add(new RuneModelDef(54f, "effect/lolths_curse"));
        out.add(new RuneModelDef(55f, "effect/longfooted"));
        out.add(new RuneModelDef(56f, "effect/looting"));
        out.add(new RuneModelDef(57f, "effect/loyalty"));
        out.add(new RuneModelDef(58f, "effect/luck_of_the_sea"));
        out.add(new RuneModelDef(59f, "effect/lure"));
        out.add(new RuneModelDef(60f, "effect/mending"));
        out.add(new RuneModelDef(61f, "effect/multi_roll"));
        out.add(new RuneModelDef(62f, "effect/mystical_enlightenment"));
        out.add(new RuneModelDef(63f, "effect/piercing"));
        out.add(new RuneModelDef(64f, "effect/potato_recovery"));
        out.add(new RuneModelDef(65f, "effect/purification"));
        out.add(new RuneModelDef(66f, "effect/renewal"));
        out.add(new RuneModelDef(67f, "effect/respiration"));
        out.add(new RuneModelDef(68f, "effect/riptide"));
        out.add(new RuneModelDef(69f, "effect/sculk_smite"));
        out.add(new RuneModelDef(70f, "effect/silk_touch"));
        out.add(new RuneModelDef(71f, "effect/soul_siphoner"));
        out.add(new RuneModelDef(72f, "effect/soul_speed"));
        out.add(new RuneModelDef(73f, "effect/stasis"));
        out.add(new RuneModelDef(74f, "effect/swift_sneak"));
        out.add(new RuneModelDef(75f, "effect/thorns"));
        out.add(new RuneModelDef(76f, "effect/vanishing_curse"));
        out.add(new RuneModelDef(77f, "effect/voltaic_shot"));
        out.add(new RuneModelDef(78f, "effect/wind_burst"));
        out.add(new RuneModelDef(79f, "effect/multishot"));
        out.add(new RuneModelDef(80f, "effect/punch"));

        return out;
    }

    private record RuneModelDef(float predicateValue, String subPath) {}
}
