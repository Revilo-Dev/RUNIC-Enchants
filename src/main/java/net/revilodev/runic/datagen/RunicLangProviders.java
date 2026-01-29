package net.revilodev.runic.datagen;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Generates:
 *  - assets/runic/lang/en_us.json
 *  - assets/runic/lang/en_gb.json
 *  - assets/runic/lang/en_au.json
 *  - assets/runic/lang/en_ca.json
 *  - assets/runic/lang/en_nz.json
 */

public final class RunicLangProviders {
    private RunicLangProviders() {}

    public static final class EN_US extends Base {
        public EN_US(PackOutput output) { super(output, "en_us"); }
    }

    public static final class EN_GB extends Base {
        public EN_GB(PackOutput output) { super(output, "en_gb"); }
    }

    public static final class EN_AU extends Base {
        public EN_AU(PackOutput output) { super(output, "en_au"); }
    }

    public static final class EN_CA extends Base {
        public EN_CA(PackOutput output) { super(output, "en_ca"); }
    }

    public static final class EN_NZ extends Base {
        public EN_NZ(PackOutput output) { super(output, "en_nz"); }
    }

    private abstract static class Base extends LanguageProvider {
        protected Base(PackOutput output, String locale) {
            super(output, "runic", locale); // MODID
        }

        protected void addTranslations() {
            add("creativetab.runicmod.runic_items", "RUNIC");

            // Items
            add("item.runic.repair_rune", "Repair Inscription");
            add("item.runic.expansion_rune", "Expansion Inscription");
            add("item.runic.upgrade_rune", "Upgrade Inscription");
            add("item.runic.nullification_rune", "Nullification Inscription");
            add("item.runic.reroll_inscription", "Reroll Inscription");
            add("item.runic.wild_inscription", "Wild Inscription");
            add("item.runic.extraction_inscription", "Extraction Inscription");
            add("item.runic.cursed_inscription", "Cursed Inscription");
            add("item.runic.blank_inscription", "Blank Inscription");
            add("item.runic.enhanced_rune", "Rune");
            add("item.runic.etching", "Etching");
            add("item.runic.blank_etching", "Etching");

            // Blocks
            add("block.runic.artisans_workbench", "Artisans Workbench");
            add("block.runic.etching_table", "Etching Table");

            // Vanilla
            add("minecraft:experience_bottle", "Bottle o' Experience");

            // Effects
            add("effect.runic.bleeding", "Bleeding");
            add("effect.runic.stunning", "Stunned");

            // Tooltip stats
            add("tooltip.runic.stat.attack_speed", "\uefe5 Attack Speed");
            add("tooltip.runic.stat.attack_damage", "\uefe5 Attack Damage");
            add("tooltip.runic.stat.attack_range", "\uefe5 Attack Range");
            add("tooltip.runic.stat.movement_speed", "\uefe4 Movement Speed");
            add("tooltip.runic.stat.sweeping_range", "\uefe5 Sweeping Range");
            add("tooltip.runic.stat.durability", "\uefe2 Durability");
            add("tooltip.runic.stat.resistance", "\uefe2 Resistance");
            add("tooltip.runic.stat.fire_resistance", "\uefe2 Fire Resistance");
            add("tooltip.runic.stat.blast_resistance", "\uefe2 Blast Resistance");
            add("tooltip.runic.stat.projectile_resistance", "\uefe2 Projectile Resistance");
            add("tooltip.runic.stat.knockback_resistance", "\uefe2 Knockback Resistance");
            add("tooltip.runic.stat.mining_speed", "\uefe6 Mining Speed");
            add("tooltip.runic.stat.swimming_speed", "\uefe7 Swimming Speed");
            add("tooltip.runic.stat.fall_reduction", "\uefe4 Fall Damage Reduction");
            add("tooltip.runic.stat.undead_damage", "\ueef3 Undead Damage");
            add("tooltip.runic.stat.nether_damage", "\ueef3 Nether Damage");
            add("tooltip.runic.stat.health", "\ueef4 Health Boost");
            add("tooltip.runic.stat.stun_chance", "\ueef4 Stun Chance");
            add("tooltip.runic.stat.flame_chance", "\ueef4 Flame Chance");
            add("tooltip.runic.stat.bleeding_chance", "\ueef4 Bleeding Chance");
            add("tooltip.runic.stat.shocking_chance", "\ueef4 Shocking Chance");
            add("tooltip.runic.stat.poison_chance", "\ueef4 Poison Chance");
            add("tooltip.runic.stat.withering_chance", "\ueef4 Withering Chance");
            add("tooltip.runic.stat.weakening_chance", "\ueef4 Weakening Chance");
            add("tooltip.runic.stat.healing_efficiency", "\ueef4 Healing Efficiency");
            add("tooltip.runic.stat.water_breathing", "\uefe1 Water Breathing");
            add("tooltip.runic.stat.draw_speed", "\uefe8 Draw Speed");
            add("tooltip.runic.stat.jump_height", "\uefe7 Jump Height");

            add("tooltip.runic.stat.toughness", "\uefe2 Armour Toughness");
            add("tooltip.runic.stat.freezing_chance", "\ueef4 Freezing Chance");
            add("tooltip.runic.stat.leeching_chance", "\ueef4 Leeching Chance");
            add("tooltip.runic.stat.looting", "\ueef3 Looting");
            add("tooltip.runic.stat.bonus_chance", "\uefe5 Bonus Shot Chance");
            add("tooltip.runic.stat.power", "\uefe5 Power");

            // Enchants
            add("enchantment.runic.poison_cloud", "Poison Cloud");
            add("enchantment.runic.pain_cycle", "Pain Cycle");
            add("enchantment.runic.committed", "Committed");

            // Inscription
            add("tooltip.runic.expansion_rune", "Adds 1 Rune slot, reduces max durability by 20%, applies Negative (max 3 uses)");
            add("tooltip.runic.repair_rune", "Repairs item to full durability, reduces max durability by 5%");
            add("tooltip.runic.nullification_rune", "Removes all enhancements, resets Rune slots, applies Negative");
            add("tooltip.runic.upgrade_rune", "Upgrades a random stat by up to 10%, costs durability equal to upgrade (min 25% durability)");
            add("tooltip.runic.reroll_inscription", "Rerolls a stat value, applies Instable");
            add("tooltip.runic.cursed_inscription", "50% over-upgrade a stat beyond max by 10%, 50% apply Cursed");
            add("tooltip.runic.wild_inscription", "Removes all enhancements, applies random enhancements, applies Cursed");
            add("tooltip.runic.extraction_inscription", "Extracts 1 enhancement into an Etching, applies Sealed");

            // tooltips
            add("tooltip.runic.use_etching_table", "Used in an Etching Table");
            add("tooltip.runic.aqua_affinity", "Increases underwater mining speed");
            add("tooltip.runic.bane_of_arthropods", "Deal more damage to arthropod type enemies");
            add("tooltip.runic.binding_curse", "Item cannot be unequipped");
            add("tooltip.runic.blast_protection", "Reduces explosion type damage");
            add("tooltip.runic.breach", "Reduces armour effectiveness");
            add("tooltip.runic.channeling", "Strikes lightning during thunderstorms");
            add("tooltip.runic.depth_strider", "Increases underwater movement speed");
            add("tooltip.runic.efficiency", "Increases mining speed");
            add("tooltip.runic.feather_falling", "Reduces fall damage");
            add("tooltip.runic.fire_aspect", "Sets targets alight on hit");
            add("tooltip.runic.fire_protection", "Reduces fire damage and burn time");
            add("tooltip.runic.flame", "Sets arrows on fire");
            add("tooltip.runic.fortune", "Increases block drops");
            add("tooltip.runic.frost_walker", "Turns water into ice");
            add("tooltip.runic.impaling", "Extra damage against aquatic mobs");
            add("tooltip.runic.infinity", "50/50 chance to not consume an arrow");
            add("tooltip.runic.knockback", "Increases knockback distance");
            add("tooltip.runic.looting", "Increases loot drops");
            add("tooltip.runic.loyalty", "Returns after being thrown");
            add("tooltip.runic.luck_of_the_sea", "Improves rarity of fishing loot");
            add("tooltip.runic.lure", "Decreases time to catch fish");
            add("tooltip.runic.mending", "Uses XP to repair the item");
            add("tooltip.runic.multishot", "Shoot an extra 2 arrows");
            add("tooltip.runic.piercing", "Arrows pass through multiple mobs");
            add("tooltip.runic.power", "Increases arrow damage");
            add("tooltip.runic.projectile_protection", "Reduces projectile damage");
            add("tooltip.runic.protection", "Reduces melee damage");
            add("tooltip.runic.punch", "Increases arrow knockback");
            add("tooltip.runic.quick_charge", "Reduces reload time");
            add("tooltip.runic.respiration", "Extends underwater breathing time");
            add("tooltip.runic.riptide", "Propels the player in water or rain");
            add("tooltip.runic.sharpness", "+1 Attack Damage per level");
            add("tooltip.runic.silk_touch", "Mined blocks drop themselves");
            add("tooltip.runic.smite", "Deals extra damage to undead");
            add("tooltip.runic.soul_speed", "Increases movement on soul blocks");
            add("tooltip.runic.sweeping_edge", "Increases sweeping attack damage");
            add("tooltip.runic.swift_sneak", "Increases sneaking speed");
            add("tooltip.runic.thorns", "Damages attackers");
            add("tooltip.runic.unbreaking", "Increases durability");
            add("tooltip.runic.vanishing_curse", "The item vanishes on death");
            add("tooltip.runic.wind_burst", "Summons a wind charge on attack");

            // descriptions
            add("tooltip.runic.skulk_smite", "Deals extra damage to sculk creatures");
            add("tooltip.runic.capacity", "Increases maximum capacity");
            add("tooltip.runic.soul_siphoner", "% chance to siphon extra soul essence");
            add("tooltip.runic.fire_react", "% chance to ignite attackers");
            add("tooltip.runic.catalysis", "Enhances potion and catalyst effects");
            add("tooltip.runic.destruction", "+% attack damage");
            add("tooltip.runic.mystical_enlightenment", "Boosts enchanting power of nearby tables");
            add("tooltip.runic.renewal", "Gradually repairs the item");
            add("tooltip.runic.chill_aura", "Slows nearby enemies with freezing aura");
            add("tooltip.runic.potato_recovery", "% chance to not consume a potato");
            add("tooltip.runic.acrobat", "Reduces dodge roll cooldown");
            add("tooltip.runic.longfooted", "Increases dodge roll distance");
            add("tooltip.runic.multi_roll", "Allows multiple rolls before cooldown");
        }
    }
}
