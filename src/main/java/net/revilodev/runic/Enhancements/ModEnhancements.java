package net.revilodev.runic.Enhancements;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.revilodev.runic.Enhancements.custom.*;
import net.revilodev.runic.RunicMod;

public class ModEnhancements {
    public static final ResourceKey<Enchantment> LIGHTNING_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "lightning_aspect"));
    public static final ResourceKey<Enchantment> POISON_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "poison_aspect"));
    public static final ResourceKey<Enchantment> SLOWNESS_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "slowness_aspect"));
    public static final ResourceKey<Enchantment> WEAKNESS_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "weakness_aspect"));
    public static final ResourceKey<Enchantment> SWIFT_STRIKE = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "swift_strike"));
    public static final ResourceKey<Enchantment> SWIFT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "swift"));
    //public static final ResourceKey<Enchantment> AIR_JUMP = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "air_jump"));
    public static final ResourceKey<Enchantment> BLEEDING_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "bleeding_aspect"));
    public static final ResourceKey<Enchantment> STUNNING_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "stunning_aspect"));
    public static final ResourceKey<Enchantment> HOARD_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "buzzing_aspect"));
    public static final ResourceKey<Enchantment> LEAPING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "leaping"));
    public static final ResourceKey<Enchantment> REACH = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "reach"));
    //public static final ResourceKey<Enchantment> SOULBOUND = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "soulbound"));
    public static final ResourceKey<Enchantment> VITALITY = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "vitality"));
    public static final ResourceKey<Enchantment> WITHER_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "wither_aspect"));
    public static final ResourceKey<Enchantment> MANDELA_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "mandela_aspect"));
    public static final ResourceKey<Enchantment> CURSEOFOOZING = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "curse_of_oozing"));
    public static final ResourceKey<Enchantment> CURSEOFUNLUCK = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "curse_of_unluck"));

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        var enchantments = context.lookup(Registries.ENCHANTMENT);
        var items = context.lookup(Registries.ITEM);

        register(context, LIGHTNING_ASPECT, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                        5, 2,
                        Enchantment.dynamicCost(5, 8),
                        Enchantment.dynamicCost(25, 8),
                        2,
                        EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, new LightningAspect()));

        register(context, POISON_ASPECT, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                        5, 2,
                        Enchantment.dynamicCost(5, 8),
                        Enchantment.dynamicCost(25, 8),
                        2,
                        EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, new PoisonAspect()));

        register(context, SLOWNESS_ASPECT, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                        5, 2,
                        Enchantment.dynamicCost(5, 8),
                        Enchantment.dynamicCost(25, 8),
                        2,
                        EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, new SlownessAspect()));

        register(context, WEAKNESS_ASPECT, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                        5, 2,
                        Enchantment.dynamicCost(5, 8),
                        Enchantment.dynamicCost(25, 8),
                        2,
                        EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, new WeaknessAspect()));

        register(context, HOARD_ASPECT, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                        5, 2,
                        Enchantment.dynamicCost(5, 8),
                        Enchantment.dynamicCost(25, 8),
                        2,
                        EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM, new HoardAspect()));

        var swift_strikeDef = Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                5, 4,
                Enchantment.dynamicCost(5, 8),
                Enchantment.dynamicCost(25, 8),
                2,
                EquipmentSlotGroup.MAINHAND
        );

        register(context, SWIFT_STRIKE, Enchantment.enchantment(swift_strikeDef)
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "swift_strike"),
                                Attributes.ATTACK_SPEED,
                                LevelBasedValue.perLevel(0.1F, 0.05F),
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        )));

        var swiftDef = Enchantment.definition(
                items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                5,
                2,
                Enchantment.dynamicCost(10, 8),
                Enchantment.dynamicCost(30, 8),
                2,
                EquipmentSlotGroup.FEET
        );

        register(context, SWIFT, Enchantment.enchantment(swiftDef)
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "swift"),
                                Attributes.MOVEMENT_SPEED,
                                LevelBasedValue.perLevel(0.15F, 0.10F),
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        )));

        var airDef = Enchantment.definition(
                items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                2,
                1,
                Enchantment.dynamicCost(20, 0),
                Enchantment.dynamicCost(50, 0),
                1,
                EquipmentSlotGroup.FEET
        );

        //register(context, AIR_JUMP, Enchantment.enchantment(airDef));

        var bleedDef = Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                5, 3,
                Enchantment.dynamicCost(10, 10),
                Enchantment.dynamicCost(40, 10),
                2,
                EquipmentSlotGroup.MAINHAND
        );

        register(context, BLEEDING_ASPECT, Enchantment.enchantment(bleedDef)
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, new BleedingAspect()));

        var stunDef = Enchantment.definition(
                items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                5, 3,
                Enchantment.dynamicCost(10, 10),
                Enchantment.dynamicCost(40, 10),
                2,
                EquipmentSlotGroup.MAINHAND
        );

        register(context, STUNNING_ASPECT, Enchantment.enchantment(stunDef)
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, new StunningAspect()));

        var leapDef = Enchantment.definition(
                items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                2, 2,
                Enchantment.dynamicCost(10, 8),
                Enchantment.dynamicCost(30, 8),
                2,
                EquipmentSlotGroup.FEET
        );

        register(context, LEAPING, Enchantment.enchantment(leapDef)
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "leaping"),
                                Attributes.JUMP_STRENGTH,
                                LevelBasedValue.perLevel(0.05F, 0.05F),
                                AttributeModifier.Operation.ADD_VALUE
                        )));

        var unluckdef = Enchantment.definition(
                items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                items.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                2, 1,
                Enchantment.dynamicCost(10, 8),
                Enchantment.dynamicCost(30, 8),
                2,
                EquipmentSlotGroup.FEET
        );

        register(context, CURSEOFUNLUCK, Enchantment.enchantment(unluckdef)
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "unluck"),
                                Attributes.LUCK,
                                LevelBasedValue.perLevel(-0.15F, -0.15F),
                                AttributeModifier.Operation.ADD_VALUE
                        )));

        var reachDef = Enchantment.definition(
                items.getOrThrow(ItemTags.MINING_ENCHANTABLE),
                items.getOrThrow(ItemTags.MINING_ENCHANTABLE),
                3, 3,
                Enchantment.dynamicCost(15, 10),
                Enchantment.dynamicCost(45, 10),
                2,
                EquipmentSlotGroup.MAINHAND
        );

        register(context, REACH, Enchantment.enchantment(reachDef)
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "reach_block"),
                                Attributes.BLOCK_INTERACTION_RANGE,
                                LevelBasedValue.perLevel(0.5F, 0.5F),
                                AttributeModifier.Operation.ADD_VALUE
                        ))
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "reach_entity"),
                                Attributes.ENTITY_INTERACTION_RANGE,
                                LevelBasedValue.perLevel(1.0F, 0.5F),
                                AttributeModifier.Operation.ADD_VALUE
                        )));

        var soulboundDef = Enchantment.definition(
                items.getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
                items.getOrThrow(ItemTags.VANISHING_ENCHANTABLE),
                1, 1,
                Enchantment.dynamicCost(30, 0),
                Enchantment.dynamicCost(60, 0),
                1,
                EquipmentSlotGroup.ANY
        );

        var vitalityDef = Enchantment.definition(
                items.getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE),
                items.getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE),
                5,
                5,
                Enchantment.dynamicCost(10, 8),
                Enchantment.dynamicCost(30, 8),
                2,
                EquipmentSlotGroup.CHEST
        );

        register(context, VITALITY, Enchantment.enchantment(vitalityDef)
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "vitality"),
                                Attributes.MAX_HEALTH,
                                LevelBasedValue.perLevel(4.0F, 4.0F),
                                AttributeModifier.Operation.ADD_VALUE
                        )
                )
        );
        register(context, WITHER_ASPECT, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                        5, 2,
                        Enchantment.dynamicCost(5, 8),
                        Enchantment.dynamicCost(25, 8),
                        2,
                        EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, new WitherAspect()));

        register(context, MANDELA_ASPECT, Enchantment.enchantment(Enchantment.definition(
                        items.getOrThrow(ItemTags.WEAPON_ENCHANTABLE),
                        items.getOrThrow(ItemTags.SWORD_ENCHANTABLE),
                        5, 2,
                        Enchantment.dynamicCost(5, 8),
                        Enchantment.dynamicCost(25, 8),
                        2,
                        EquipmentSlotGroup.MAINHAND))
                .exclusiveWith(enchantments.getOrThrow(EnchantmentTags.DAMAGE_EXCLUSIVE))
                .withEffect(EnchantmentEffectComponents.POST_ATTACK, EnchantmentTarget.ATTACKER, EnchantmentTarget.VICTIM, new Mandela_Aspect()));

        var curseofoozingdef = Enchantment.definition(
                items.getOrThrow(ItemTags.ARMOR_ENCHANTABLE),
                items.getOrThrow(ItemTags.CHEST_ARMOR_ENCHANTABLE),
                5,
                2,
                Enchantment.dynamicCost(10, 8),
                Enchantment.dynamicCost(30, 8),
                2,
                EquipmentSlotGroup.CHEST
        );

        register(context, CURSEOFOOZING, Enchantment.enchantment(vitalityDef)
                .withEffect(EnchantmentEffectComponents.ATTRIBUTES,
                        new EnchantmentAttributeEffect(
                                ResourceLocation.tryParse(RunicMod.MOD_ID + "curse_of_oozing"),
                                Attributes.ARMOR,
                                LevelBasedValue.perLevel(0.15F, 0.10F),
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        )));

        //register(context, SOULBOUND, Enchantment.enchantment(soulboundDef));
    }

    private static void register(BootstrapContext<Enchantment> registry, ResourceKey<Enchantment> key, Enchantment.Builder builder) {
        registry.register(key, builder.build(key.location()));
    }
}
