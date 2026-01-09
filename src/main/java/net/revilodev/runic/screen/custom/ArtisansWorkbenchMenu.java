package net.revilodev.runic.screen.custom;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.revilodev.runic.block.ModBlocks;
import net.revilodev.runic.gear.GearAttribute;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.registry.ModDataComponents;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.*;

public final class ArtisansWorkbenchMenu extends AbstractContainerMenu {
    public static final int BUTTON_FORGE = 0;

    private static final String ROOT = "runic";
    private static final String PREVIEW_DELTA = "preview_delta";
    private static final int MAX_ATTR_LEVEL = 10;

    private static final String OBF_MODE = "obf_mode";
    private static final String OBF_TEXT = "obf_text";
    private static final String OBF_ENH = "obf_enh";
    private static final String OBF_ATTR = "obf_attr";
    private static final String OBF_SWAP = "obf_swap";

    private final ContainerLevelAccess access;
    private final Level level;

    private final SimpleContainer input = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            ArtisansWorkbenchMenu.this.slotsChanged(this);
        }
    };

    private final ResultContainer preview = new ResultContainer();
    private static final Random RNG = new Random();

    private ArtisansWorkbenchMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(net.revilodev.runic.screen.ModMenuTypes.ARTISANS_WORKBENCH.get(), id);
        this.access = access;
        this.level = inv.player.level();

        this.addSlot(new Slot(input, 0, 26, 53) {
            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        this.addSlot(new Slot(input, 1, 80, 53) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(preview, 0, 2000, 2000) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });

        int x = 8;
        int y = 84;

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                this.addSlot(new Slot(inv, c + r * 9 + 9, x + c * 18, y + r * 18));

        for (int c = 0; c < 9; c++)
            this.addSlot(new Slot(inv, c, x + c * 18, y + 58));

        updatePreview();
    }

    public static ArtisansWorkbenchMenu server(int id, Inventory inv, Level level, BlockPos pos) {
        return new ArtisansWorkbenchMenu(id, inv, ContainerLevelAccess.create(level, pos));
    }

    public static ArtisansWorkbenchMenu client(int id, Inventory inv, BlockPos pos) {
        return new ArtisansWorkbenchMenu(id, inv, ContainerLevelAccess.create(inv.player.level(), pos));
    }

    public ItemStack getPreviewStack() {
        return this.preview.getItem(0);
    }

    public ItemStack getEnhancementStack() {
        return this.input.getItem(0);
    }

    public ItemStack getGearStack() {
        return this.input.getItem(1);
    }

    @Override
    public void slotsChanged(Container container) {
        updatePreview();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_FORGE) return super.clickMenuButton(player, id);
        if (this.level.isClientSide) return true;

        ItemStack enh = this.input.getItem(0);
        ItemStack gear = this.input.getItem(1);
        if (enh.isEmpty() || gear.isEmpty()) return false;

        if (GearAttributes.has(gear, GearAttribute.SEALED)) return false;

        if (enh.is(ModItems.EXTRACTION_INSCRIPTION.get())) {
            ItemStack extracted = createExtractionEtching(gear);
            if (!extracted.isEmpty()) {
                if (!player.getInventory().add(extracted)) {
                    player.drop(extracted, false);
                }
            }
        }

        ItemStack applied;

        if (enh.is(ModItems.WILD_INSCRIPTION.get()) || enh.is(ModItems.CURSED_INSCRIPTION.get())) {
            applied = gear.copy();
            applied.setCount(1);
            applyRuneOnTake(applied, enh);
            stripPreviewDelta(applied);
            if (ItemStack.isSameItemSameComponents(gear, applied) && gear.getDamageValue() == applied.getDamageValue()) return false;
        } else {
            ItemStack outView = this.preview.getItem(0);
            if (outView.isEmpty()) return false;

            applied = outView.copy();
            applied.setCount(1);
            stripPreviewDelta(applied);
        }

        this.input.setItem(1, applied);
        consumeEnhancement();
        playUseSound();

        this.input.setChanged();
        updatePreview();
        return true;
    }

    private void updatePreview() {
        if (this.level.isClientSide) return;

        ItemStack enh = input.getItem(0);
        ItemStack gear = input.getItem(1);

        ItemStack out = computePreview(gear, enh);
        preview.setItem(0, out);
        broadcastChanges();
    }

    private void consumeEnhancement() {
        ItemStack enh = input.getItem(0);
        if (!enh.isEmpty()) {
            enh.shrink(1);
            if (enh.isEmpty()) input.setItem(0, ItemStack.EMPTY);
        }
    }

    private void playUseSound() {
        this.access.execute((lvl, pos) ->
                lvl.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1f, 1f)
        );
    }

    private static ItemEnchantments getEnhancementEnchantments(ItemStack stack) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return !stored.isEmpty() ? stored : direct;
    }

    private static boolean isEnhancementItem(ItemStack stack) {
        return stack.getItem() instanceof RuneItem || stack.getItem() instanceof EtchingItem;
    }

    private static boolean isEtching(ItemStack stack) {
        return stack.getItem() instanceof EtchingItem;
    }

    private static boolean isInscription(ItemStack stack) {
        return stack.is(ModItems.REPAIR_INSCRIPTION.get())
                || stack.is(ModItems.EXPANSION_INSCRIPTION.get())
                || stack.is(ModItems.NULLIFICATION_INSCRIPTION.get())
                || stack.is(ModItems.UPGRADE_INSCRIPTION.get())
                || stack.is(ModItems.REROLL_INSCRIPTION.get())
                || stack.is(ModItems.CURSED_INSCRIPTION.get())
                || stack.is(ModItems.WILD_INSCRIPTION.get())
                || stack.is(ModItems.EXTRACTION_INSCRIPTION.get());
    }

    private int effectiveCapacity(ItemStack stack) {
        int cap = RuneSlots.capacity(stack);
        int neg = GearAttributes.getLevel(stack, GearAttribute.NEGATIVE);
        return Math.max(0, cap - neg);
    }

    private int effectiveRemaining(ItemStack stack) {
        int cap = effectiveCapacity(stack);
        int used = RuneSlots.used(stack);
        return Math.max(0, cap - used);
    }

    private RuneStatType getEnhancementStatType(ItemStack enhancement) {
        RuneStats stats = RuneStats.get(enhancement);
        if (stats == null || stats.isEmpty()) return null;
        for (RuneStatType t : stats.view().keySet()) return t;
        return null;
    }

    private boolean canApplyStatTo(ItemStack target, RuneStatType stat) {
        if (stat == null) return false;

        RuneStats existing = RuneStats.get(target);
        if (existing != null && existing.has(stat)) return false;

        Item item = target.getItem();

        return switch (stat) {
            case ATTACK_DAMAGE, ATTACK_SPEED, ATTACK_RANGE, SWEEPING_RANGE,
                 UNDEAD_DAMAGE, NETHER_DAMAGE,
                 STUN_CHANCE, FLAME_CHANCE, BLEEDING_CHANCE, SHOCKING_CHANCE,
                 POISON_CHANCE, WITHERING_CHANCE, WEAKENING_CHANCE,
                 FREEZING_CHANCE, LEECHING_CHANCE, BONUS_CHANCE ->
                    item instanceof SwordItem
                            || item instanceof AxeItem
                            || item instanceof TridentItem
                            || item instanceof MaceItem;

            case POWER, DRAW_SPEED ->
                    item instanceof BowItem || item instanceof CrossbowItem;

            case MINING_SPEED ->
                    item instanceof DiggerItem;

            case MOVEMENT_SPEED, HEALTH, RESISTANCE, FIRE_RESISTANCE, BLAST_RESISTANCE,
                 PROJECTILE_RESISTANCE, KNOCKBACK_RESISTANCE,
                 TOUGHNESS, HEALING_EFFICIENCY, JUMP_HEIGHT ->
                    item instanceof ArmorItem;

            case DURABILITY ->
                    target.isDamageableItem();

            default -> true;
        };
    }

    private boolean canApplyAnyEffectEnchant(ItemStack target, ItemEnchantments enchants) {
        if (enchants == null || enchants.isEmpty()) return false;

        ItemEnchantments current = target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        for (Object2IntMap.Entry<Holder<Enchantment>> e : enchants.entrySet()) {
            Holder<Enchantment> h = e.getKey();
            if (!RuneItem.isEffectEnchantment(h)) continue;

            int desired = RuneItem.clampEffectLevel(h, e.getIntValue());
            int have = current.getLevel(h);
            if (have >= desired) continue;

            if (have == 0) {
                List<Holder<Enchantment>> existing = new ArrayList<>(current.keySet());
                if (!EnchantmentHelper.isEnchantmentCompatible(existing, h)) continue;
            }

            return true;
        }

        return false;
    }

    private boolean canApplyNullification(ItemStack target) {
        if (GearAttributes.getLevel(target, GearAttribute.NEGATIVE) >= MAX_ATTR_LEVEL) return false;

        boolean hasAnyEnchants =
                !target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
                        || !target.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();

        boolean hasUsedSlots = RuneSlots.used(target) > 0 || target.has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

        boolean hasAnyStats = false;
        RuneStats current = RuneStats.get(target);
        if (current != null && !current.isEmpty()) {
            for (float v : current.view().values()) {
                if (v != 0.0F) {
                    hasAnyStats = true;
                    break;
                }
            }
        }

        return hasAnyEnchants || hasUsedSlots || hasAnyStats;
    }

    private boolean canApplyUpgrade(ItemStack target) {
        if (!target.isDamageableItem()) return false;

        RuneStats current = RuneStats.get(target);
        if (current == null || current.isEmpty()) return false;

        float curseMult = GearAttributes.cursedMultiplier(target);

        int max = target.getMaxDamage();
        int dmg = target.getDamageValue();
        int minRemaining = (int) Math.ceil(max * 0.25D);
        int maxCost = (max - minRemaining) - dmg;
        if (maxCost < 1) return false;

        for (Map.Entry<RuneStatType, Float> e : current.view().entrySet()) {
            RuneStatType t = e.getKey();
            float v = e.getValue();
            if (v <= 0.0F) continue;

            float cap = t.cap();
            if (cap <= 0.0F) continue;

            float capEff = cap * curseMult;
            int byCap = (int) Math.floor(capEff - v + 1e-6);
            int allowed = Math.min(10, Math.min(byCap, maxCost));
            if (allowed >= 1) return true;
        }

        return false;
    }

    private boolean canApplyRepair(ItemStack target) {
        if (!target.isDamageableItem()) return false;
        if (target.getMaxDamage() <= 1) return false;
        return target.getDamageValue() > 0;
    }

    private boolean canApplyExpansion(ItemStack target) {
        if (!target.isDamageableItem()) return false;
        if (target.getMaxDamage() <= 1) return false;
        return RuneSlots.expansionsUsed(target) < 3;
    }

    private boolean canApplyReroll(ItemStack target) {
        if (GearAttributes.getLevel(target, GearAttribute.INSTABLE) >= MAX_ATTR_LEVEL) return false;

        RuneStats st = RuneStats.get(target);
        if (st == null || st.isEmpty()) return false;
        for (float v : st.view().values()) if (v != 0.0F) return true;
        return false;
    }

    private boolean canApplyCursed(ItemStack target) {
        return GearAttributes.getLevel(target, GearAttribute.CURSED) < MAX_ATTR_LEVEL;
    }

    private boolean canApplyWild(ItemStack target) {
        return GearAttributes.getLevel(target, GearAttribute.CURSED) < MAX_ATTR_LEVEL;
    }

    private boolean canApplyExtraction(ItemStack target) {
        if (GearAttributes.getLevel(target, GearAttribute.SEALED) >= MAX_ATTR_LEVEL) return false;

        if (!target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()) return true;

        RuneStats st = RuneStats.get(target);
        if (st != null && !st.isEmpty()) {
            for (float v : st.view().values()) if (v != 0.0F) return true;
        }

        return false;
    }

    private ItemStack computePreview(ItemStack gear, ItemStack enhancement) {
        if (gear.isEmpty() || enhancement.isEmpty()) return ItemStack.EMPTY;
        if (!isEnhancementItem(enhancement) && !isInscription(enhancement)) return ItemStack.EMPTY;
        if (GearAttributes.has(gear, GearAttribute.SEALED)) return ItemStack.EMPTY;

        if (enhancement.is(ModItems.WILD_INSCRIPTION.get())) {
            if (!canApplyWild(gear)) return ItemStack.EMPTY;
            ItemStack out = gear.copy();
            out.setCount(1);
            writeObfuscatedPreview(out, "wild", true, true, false);
            return out;
        }

        if (enhancement.is(ModItems.CURSED_INSCRIPTION.get())) {
            if (!canApplyCursed(gear)) return ItemStack.EMPTY;
            ItemStack out = gear.copy();
            out.setCount(1);
            writeObfuscatedPreview(out, "cursed", true, true, true);
            return out;
        }

        ItemStack base = gear.copy();
        base.setCount(1);

        ItemStack out = gear.copy();
        out.setCount(1);

        if (enhancement.is(ModItems.REPAIR_INSCRIPTION.get())) {
            if (!canApplyRepair(out)) return ItemStack.EMPTY;
            applyRuneOnTake(out, enhancement);
        } else if (enhancement.is(ModItems.EXPANSION_INSCRIPTION.get())) {
            if (!canApplyExpansion(out)) return ItemStack.EMPTY;
            applyRuneOnTake(out, enhancement);
        } else if (enhancement.is(ModItems.NULLIFICATION_INSCRIPTION.get())) {
            if (!canApplyNullification(out)) return ItemStack.EMPTY;
            applyRuneOnTake(out, enhancement);
        } else if (enhancement.is(ModItems.UPGRADE_INSCRIPTION.get())) {
            if (!canApplyUpgrade(out)) return ItemStack.EMPTY;
            applyRuneOnTake(out, enhancement);
        } else if (enhancement.is(ModItems.REROLL_INSCRIPTION.get())) {
            if (!canApplyReroll(out)) return ItemStack.EMPTY;
            applyRuneOnTake(out, enhancement);
        } else if (enhancement.is(ModItems.EXTRACTION_INSCRIPTION.get())) {
            if (!canApplyExtraction(out)) return ItemStack.EMPTY;
            applyRuneOnTake(out, enhancement);
        } else {
            if (!isEnhancementItem(enhancement)) return ItemStack.EMPTY;
            if (effectiveRemaining(out) <= 0) return ItemStack.EMPTY;

            RuneStats stats = RuneStats.get(enhancement);
            RuneStatType stat = (stats != null && !stats.isEmpty()) ? getEnhancementStatType(enhancement) : null;
            boolean statApplicable = stat != null && canApplyStatTo(out, stat);

            ItemEnchantments enchants = getEnhancementEnchantments(enhancement);
            boolean effectApplicable = canApplyAnyEffectEnchant(out, enchants);

            if (!statApplicable && !effectApplicable) return ItemStack.EMPTY;

            applyRuneOnTake(out, enhancement);
        }

        boolean unchanged = ItemStack.isSameItemSameComponents(base, out) && base.getDamageValue() == out.getDamageValue();
        if (unchanged) return ItemStack.EMPTY;

        writePreviewDelta(base, out);
        return out;
    }

    private static CompoundTag getRootCopy(ItemStack stack) {
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return cd.copyTag();
    }

    private static void setRoot(ItemStack stack, CompoundTag root) {
        if (root == null || root.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static void stripPreviewDelta(ItemStack stack) {
        CompoundTag root = getRootCopy(stack);
        if (!root.contains(ROOT, Tag.TAG_COMPOUND)) return;

        CompoundTag runic = root.getCompound(ROOT);
        runic.remove(PREVIEW_DELTA);

        if (runic.isEmpty()) root.remove(ROOT);
        else root.put(ROOT, runic);

        setRoot(stack, root);
    }

    private void writeObfuscatedPreview(ItemStack out, String mode, boolean enh, boolean attr, boolean swap) {
        CompoundTag delta = new CompoundTag();
        delta.putString(OBF_MODE, mode);
        delta.putString(OBF_TEXT, "????????????");
        delta.putBoolean(OBF_ENH, enh);
        delta.putBoolean(OBF_ATTR, attr);
        delta.putBoolean(OBF_SWAP, swap);

        CompoundTag root = getRootCopy(out);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
        runic.put(PREVIEW_DELTA, delta);
        root.put(ROOT, runic);
        setRoot(out, root);
    }

    private void writePreviewDelta(ItemStack base, ItemStack out) {
        CompoundTag delta = new CompoundTag();

        if (out.isDamageableItem()) {
            int baseMax = base.getMaxDamage();
            int outMax = out.getMaxDamage();
            int baseRem = baseMax > 0 ? (baseMax - base.getDamageValue()) : 0;
            int outRem = outMax > 0 ? (outMax - out.getDamageValue()) : 0;

            int dMax = outMax - baseMax;
            int dRem = outRem - baseRem;

            if (dMax != 0) delta.putInt("dur_max", dMax);
            if (dRem != 0) delta.putInt("dur_rem", dRem);
        }

        RuneStats bStats = RuneStats.get(base);
        RuneStats oStats = RuneStats.get(out);

        EnumMap<RuneStatType, Float> bMap = new EnumMap<>(RuneStatType.class);
        EnumMap<RuneStatType, Float> oMap = new EnumMap<>(RuneStatType.class);

        if (bStats != null && !bStats.isEmpty()) bMap.putAll(bStats.view());
        if (oStats != null && !oStats.isEmpty()) oMap.putAll(oStats.view());

        CompoundTag statDelta = new CompoundTag();
        for (RuneStatType t : RuneStatType.values()) {
            float bv = bMap.getOrDefault(t, 0.0F);
            float ov = oMap.getOrDefault(t, 0.0F);
            float dv = ov - bv;
            if (Math.abs(dv) > 0.0001F) statDelta.putFloat(t.id(), dv);
        }
        if (!statDelta.isEmpty()) delta.put("rune_stats", statDelta);

        int baseCap = Math.max(0, RuneSlots.capacity(base) - GearAttributes.getLevel(base, GearAttribute.NEGATIVE));
        int outCap = Math.max(0, RuneSlots.capacity(out) - GearAttributes.getLevel(out, GearAttribute.NEGATIVE));
        int dCap = outCap - baseCap;
        if (dCap != 0) delta.putInt("slot_cap", dCap);

        int dUsed = RuneSlots.used(out) - RuneSlots.used(base);
        if (dUsed != 0) delta.putInt("slot_used", dUsed);

        CompoundTag attrDelta = new CompoundTag();
        for (GearAttribute a : GearAttribute.values()) {
            int dv = GearAttributes.getLevel(out, a) - GearAttributes.getLevel(base, a);
            if (dv != 0) attrDelta.putInt(a.id(), dv);
        }
        if (!attrDelta.isEmpty()) delta.put("attrs", attrDelta);

        CompoundTag itemStats = computeItemStatsDelta(base, out);
        if (!itemStats.isEmpty()) delta.put("item_stats", itemStats);

        if (delta.isEmpty()) return;

        CompoundTag root = getRootCopy(out);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
        runic.put(PREVIEW_DELTA, delta);
        root.put(ROOT, runic);
        setRoot(out, root);
    }

    private CompoundTag computeItemStatsDelta(ItemStack base, ItemStack out) {
        CompoundTag t = new CompoundTag();

        Item item = out.getItem();

        boolean isSword = item instanceof SwordItem;
        boolean isTrident = item instanceof TridentItem;
        boolean isBowLike = item instanceof BowItem || item instanceof CrossbowItem;
        boolean isMace = item instanceof MaceItem;

        boolean isWeapon =
                isSword ||
                        isMace ||
                        item instanceof AxeItem ||
                        isTrident ||
                        isBowLike;

        boolean isToolForMining =
                item instanceof PickaxeItem ||
                        item instanceof ShovelItem ||
                        item instanceof AxeItem ||
                        item instanceof HoeItem;

        boolean isArmor = item instanceof ArmorItem;

        if (isArmor) {
            ArmorItem armor = (ArmorItem) item;
            EquipmentSlot slot = armor.getEquipmentSlot();

            class ArmorVals {
                double armor = 0;
                double toughBase = 0;
                double toughMult = 0;
                double knockBase = 0;
                double knockMult = 0;
                double move = 0;
                double soul = 0;
                double healthBase = 0;
                double healthMult = 0;
            }

            ArmorVals b = new ArmorVals();
            ArmorVals o = new ArmorVals();

            base.forEachModifier(slot, (Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, AttributeModifier mod) -> {
                if (attr.is(Attributes.ARMOR)) b.armor += mod.amount();
                else if (attr.is(Attributes.ARMOR_TOUGHNESS)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) b.toughMult += mod.amount();
                    else b.toughBase += mod.amount();
                } else if (attr.is(Attributes.KNOCKBACK_RESISTANCE)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) b.knockMult += mod.amount();
                    else b.knockBase += mod.amount();
                } else if (attr.is(Attributes.MOVEMENT_SPEED)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) b.move += mod.amount() * 100.0;
                    else b.move += mod.amount();
                } else if (attr.is(Attributes.MOVEMENT_EFFICIENCY)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) b.soul += mod.amount() * 100.0;
                    else b.soul += mod.amount();
                } else if (attr.is(Attributes.MAX_HEALTH)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) b.healthMult += mod.amount();
                    else b.healthBase += mod.amount();
                }
            });

            out.forEachModifier(slot, (Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, AttributeModifier mod) -> {
                if (attr.is(Attributes.ARMOR)) o.armor += mod.amount();
                else if (attr.is(Attributes.ARMOR_TOUGHNESS)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) o.toughMult += mod.amount();
                    else o.toughBase += mod.amount();
                } else if (attr.is(Attributes.KNOCKBACK_RESISTANCE)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) o.knockMult += mod.amount();
                    else o.knockBase += mod.amount();
                } else if (attr.is(Attributes.MOVEMENT_SPEED)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) o.move += mod.amount() * 100.0;
                    else o.move += mod.amount();
                } else if (attr.is(Attributes.MOVEMENT_EFFICIENCY)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) o.soul += mod.amount() * 100.0;
                    else o.soul += mod.amount();
                } else if (attr.is(Attributes.MAX_HEALTH)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) o.healthMult += mod.amount();
                    else o.healthBase += mod.amount();
                }
            });

            double bTough = b.toughBase * (1.0 + b.toughMult);
            double oTough = o.toughBase * (1.0 + o.toughMult);

            double bKnock = b.knockBase * (1.0 + b.knockMult);
            double oKnock = o.knockBase * (1.0 + o.knockMult);

            double bHealth = b.healthBase * (1.0 + b.healthMult);
            double oHealth = o.healthBase * (1.0 + o.healthMult);

            putDelta(t, "armor", o.armor - b.armor);
            putDelta(t, "toughness", oTough - bTough);
            putDelta(t, "knockback_resistance", oKnock - bKnock);
            putDelta(t, "max_health", oHealth - bHealth);
            putDelta(t, "movement_speed", o.move - b.move);
            putDelta(t, "movement_efficiency", o.soul - b.soul);

            return t;
        }

        if (isWeapon) {
            class WeaponVals {
                double baseDmg = 0.0;
                double dmgMult = 0.0;
                double baseSpd = 0.0;
                double spdMult = 0.0;

                double damage() { return baseDmg * (1.0 + dmgMult); }
                double speed() { return (baseSpd + 4.0) * (1.0 + spdMult); }
            }

            WeaponVals b = new WeaponVals();
            WeaponVals o = new WeaponVals();

            base.forEachModifier(EquipmentSlot.MAINHAND, (Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, AttributeModifier mod) -> {
                if (attr.is(Attributes.ATTACK_DAMAGE)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) b.dmgMult += mod.amount();
                    else b.baseDmg += mod.amount();
                } else if (attr.is(Attributes.ATTACK_SPEED)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) b.spdMult += mod.amount();
                    else b.baseSpd += mod.amount();
                }
            });

            out.forEachModifier(EquipmentSlot.MAINHAND, (Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, AttributeModifier mod) -> {
                if (attr.is(Attributes.ATTACK_DAMAGE)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) o.dmgMult += mod.amount();
                    else o.baseDmg += mod.amount();
                } else if (attr.is(Attributes.ATTACK_SPEED)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) o.spdMult += mod.amount();
                    else o.baseSpd += mod.amount();
                }
            });

            double bDmg = b.damage();
            double oDmg = o.damage();
            double bSpd = b.speed();
            double oSpd = o.speed();

            putDelta(t, "attack_damage", oDmg - bDmg);
            putDelta(t, "attack_speed", oSpd - bSpd);

            double baseRange = 3.0;

            RuneStats brs = RuneItem.getRolledStatsForTooltip(base);
            RuneStats ors = RuneItem.getRolledStatsForTooltip(out);

            float bRangePct = brs != null ? brs.get(RuneStatType.ATTACK_RANGE) : 0.0F;
            float oRangePct = ors != null ? ors.get(RuneStatType.ATTACK_RANGE) : 0.0F;

            double bRange = baseRange * (1.0 + (bRangePct / 100.0));
            double oRange = baseRange * (1.0 + (oRangePct / 100.0));

            putDelta(t, "range", oRange - bRange);

            if (isBowLike) {
                float bDrawPct = brs != null ? brs.get(RuneStatType.DRAW_SPEED) : 0.0F;
                float oDrawPct = ors != null ? ors.get(RuneStatType.DRAW_SPEED) : 0.0F;

                float bPowerPct = brs != null ? brs.get(RuneStatType.POWER) : 0.0F;
                float oPowerPct = ors != null ? ors.get(RuneStatType.POWER) : 0.0F;

                double baseDraw = 1.0;
                double basePower = 1.0;

                double bDraw = baseDraw * (1.0 + (bDrawPct / 100.0));
                double oDraw = baseDraw * (1.0 + (oDrawPct / 100.0));

                double bPow = basePower * (1.0 + (bPowerPct / 100.0));
                double oPow = basePower * (1.0 + (oPowerPct / 100.0));

                putDelta(t, "draw_speed", oDraw - bDraw);
                putDelta(t, "power", oPow - bPow);
            }

            return t;
        }

        if (isToolForMining && !(item instanceof BowItem) && !(item instanceof CrossbowItem)) {
            double bBase;
            if (item instanceof PickaxeItem) bBase = base.getDestroySpeed(Blocks.STONE.defaultBlockState());
            else if (item instanceof ShovelItem) bBase = base.getDestroySpeed(Blocks.DIRT.defaultBlockState());
            else if (item instanceof AxeItem) bBase = base.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState());
            else if (item instanceof HoeItem) bBase = base.getDestroySpeed(Blocks.WHEAT.defaultBlockState());
            else bBase = base.getDestroySpeed(Blocks.STONE.defaultBlockState());

            double oBase;
            if (item instanceof PickaxeItem) oBase = out.getDestroySpeed(Blocks.STONE.defaultBlockState());
            else if (item instanceof ShovelItem) oBase = out.getDestroySpeed(Blocks.DIRT.defaultBlockState());
            else if (item instanceof AxeItem) oBase = out.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState());
            else if (item instanceof HoeItem) oBase = out.getDestroySpeed(Blocks.WHEAT.defaultBlockState());
            else oBase = out.getDestroySpeed(Blocks.STONE.defaultBlockState());

            RuneStats brs = RuneItem.getRolledStatsForTooltip(base);
            RuneStats ors = RuneItem.getRolledStatsForTooltip(out);

            float bPct = brs != null ? brs.get(RuneStatType.MINING_SPEED) : 0.0F;
            float oPct = ors != null ? ors.get(RuneStatType.MINING_SPEED) : 0.0F;

            double bFinal = bBase * (1.0 + (bPct / 100.0));
            double oFinal = oBase * (1.0 + (oPct / 100.0));

            putDelta(t, "block_break_speed", oFinal - bFinal);
        }

        return t;
    }

    private static void putDelta(CompoundTag tag, String key, double delta) {
        if (Math.abs(delta) > 1e-6) tag.putDouble(key, delta);
    }

    private static boolean reduceMaxDurability(ItemStack stack, double fraction) {
        if (!stack.isDamageableItem()) return false;

        int max = stack.getMaxDamage();
        if (max <= 1) return false;

        int newMax = (int) Math.floor(max * (1.0D - fraction));
        if (newMax >= max) newMax = max - 1;
        if (newMax < 1) newMax = 1;

        if (newMax == max) return false;

        stack.set(DataComponents.MAX_DAMAGE, newMax);

        int dmg = stack.getDamageValue();
        if (dmg >= newMax) stack.set(DataComponents.DAMAGE, newMax - 1);

        return true;
    }

    private void updateGlintAfter(ItemStack stack) {
        RuneStats stats = RuneStats.get(stack);
        ItemEnchantments ex = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if ((stats == null || stats.isEmpty()) && ex.isEmpty()) {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }
    }

    private void applyNullification(ItemStack taken) {
        taken.remove(DataComponents.ENCHANTMENTS);
        taken.remove(DataComponents.STORED_ENCHANTMENTS);
        taken.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

        RuneStats.set(taken, RuneStats.empty());
        taken.set(ModDataComponents.RUNE_SLOTS_USED.get(), 0);

        updateGlintAfter(taken);
    }

    private void applyExpansion(ItemStack taken) {
        if (RuneSlots.expansionsUsed(taken) >= 3) return;
        RuneSlots.addOneSlot(taken);
        RuneSlots.incrementExpansion(taken);
    }

    private void applyCursedDelta(ItemStack stack, int deltaLevels) {
        if (deltaLevels <= 0) return;

        RuneStats st = RuneStats.get(stack);
        if (st != null && !st.isEmpty()) {
            float mult = (float) Math.pow(0.95D, deltaLevels);
            EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
            map.putAll(st.view());
            for (Map.Entry<RuneStatType, Float> e : map.entrySet()) {
                float v = e.getValue();
                if (v != 0.0F) map.put(e.getKey(), v * mult);
            }
            RuneStats.set(stack, new RuneStats(map));
        }

        int cursed = GearAttributes.getLevel(stack, GearAttribute.CURSED);
        GearAttributes.setCursedAppliedLevel(stack, cursed);
        updateGlintAfter(stack);
    }

    private int instableShift(ItemStack stack) {
        int lvl = GearAttributes.getLevel(stack, GearAttribute.INSTABLE);
        return Math.max(0, lvl) * 2;
    }

    private float rollBaseStat(RuneStatType type, boolean etching, int instableShift) {
        int min = etching ? type.etchingMinPercent() : type.minPercent();
        int max = etching ? type.etchingMaxPercent() : type.maxPercent();

        int minAdj = Math.max(0, min - instableShift);
        int maxAdj = Math.max(minAdj, max - instableShift);

        if (minAdj == maxAdj) return (float) minAdj;
        return (float) (minAdj + RNG.nextInt(maxAdj - minAdj + 1));
    }

    private boolean applyUpgrade(ItemStack taken) {
        if (!taken.isDamageableItem()) return false;

        RuneStats st = RuneStats.get(taken);
        if (st == null || st.isEmpty()) return false;

        float curseMult = GearAttributes.cursedMultiplier(taken);

        int max = taken.getMaxDamage();
        int dmg = taken.getDamageValue();
        int minRemaining = (int) Math.ceil(max * 0.25D);
        int maxCost = (max - minRemaining) - dmg;
        if (maxCost < 1) return false;

        List<RuneStatType> candidates = new ArrayList<>();
        Map<RuneStatType, Integer> allowed = new EnumMap<>(RuneStatType.class);

        for (Map.Entry<RuneStatType, Float> e : st.view().entrySet()) {
            RuneStatType t = e.getKey();
            float v = e.getValue();
            if (v <= 0.0F) continue;

            float cap = t.cap();
            if (cap <= 0.0F) continue;

            float capEff = cap * curseMult;
            int byCap = (int) Math.floor(capEff - v + 1e-6);
            int a = Math.min(10, Math.min(byCap, maxCost));
            if (a >= 1) {
                candidates.add(t);
                allowed.put(t, a);
            }
        }

        if (candidates.isEmpty()) return false;

        RuneStatType chosen = candidates.get(RNG.nextInt(candidates.size()));
        int maxInc = allowed.getOrDefault(chosen, 0);
        if (maxInc < 1) return false;

        int inc = 1 + RNG.nextInt(maxInc);

        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        map.putAll(st.view());

        float cur = map.getOrDefault(chosen, 0.0F);
        map.put(chosen, cur + inc);

        RuneStats.set(taken, new RuneStats(map));
        taken.set(DataComponents.DAMAGE, dmg + inc);
        taken.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        updateGlintAfter(taken);
        return true;
    }

    private boolean applyReroll(ItemStack taken) {
        if (GearAttributes.getLevel(taken, GearAttribute.INSTABLE) >= MAX_ATTR_LEVEL) return false;

        RuneStats st = RuneStats.get(taken);
        if (st == null || st.isEmpty()) return false;

        List<RuneStatType> options = new ArrayList<>();
        for (Map.Entry<RuneStatType, Float> e : st.view().entrySet()) {
            if (e.getValue() != 0.0F) options.add(e.getKey());
        }
        if (options.isEmpty()) return false;

        GearAttributes.addLevel(taken, GearAttribute.INSTABLE, 1);

        RuneStatType chosen = options.get(RNG.nextInt(options.size()));
        int shift = instableShift(taken);
        float base = rollBaseStat(chosen, false, shift);
        float val = base * GearAttributes.cursedMultiplier(taken);

        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        map.putAll(st.view());
        map.put(chosen, val);

        RuneStats.set(taken, new RuneStats(map));
        taken.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        updateGlintAfter(taken);
        return true;
    }

    private boolean applyCursedInscription(ItemStack taken) {
        if (GearAttributes.getLevel(taken, GearAttribute.CURSED) >= MAX_ATTR_LEVEL) return false;

        RuneStats st = RuneStats.get(taken);

        boolean canOver = false;
        if (st != null && !st.isEmpty()) {
            for (Map.Entry<RuneStatType, Float> e : st.view().entrySet()) {
                if (e.getValue() == 0.0F) continue;
                if (e.getKey().cap() > 0.0F) {
                    canOver = true;
                    break;
                }
            }
        }

        if (canOver && RNG.nextBoolean()) {
            List<RuneStatType> opts = new ArrayList<>();
            for (Map.Entry<RuneStatType, Float> e : st.view().entrySet()) {
                if (e.getValue() == 0.0F) continue;
                if (e.getKey().cap() > 0.0F) opts.add(e.getKey());
            }
            if (opts.isEmpty()) return false;

            RuneStatType chosen = opts.get(RNG.nextInt(opts.size()));
            float cap = chosen.cap();
            float val = (cap * 1.10F) * GearAttributes.cursedMultiplier(taken);

            EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
            map.putAll(st.view());
            map.put(chosen, val);

            RuneStats.set(taken, new RuneStats(map));
            taken.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

            updateGlintAfter(taken);
            return true;
        } else {
            int before = GearAttributes.getLevel(taken, GearAttribute.CURSED);
            if (before >= MAX_ATTR_LEVEL) return false;

            GearAttributes.addLevel(taken, GearAttribute.CURSED, 1);
            applyCursedDelta(taken, 1);
            GearAttributes.setCursedAppliedLevel(taken, Math.min(MAX_ATTR_LEVEL, before + 1));
            return true;
        }
    }

    private boolean applyWild(ItemStack taken) {
        if (GearAttributes.getLevel(taken, GearAttribute.CURSED) >= MAX_ATTR_LEVEL) return false;

        applyNullification(taken);

        int before = GearAttributes.getLevel(taken, GearAttribute.CURSED);
        if (before >= MAX_ATTR_LEVEL) return false;

        GearAttributes.addLevel(taken, GearAttribute.CURSED, 1);
        applyCursedDelta(taken, 1);

        int cap = effectiveCapacity(taken);
        if (cap <= 0) {
            updateGlintAfter(taken);
            return true;
        }

        float curseMult = GearAttributes.cursedMultiplier(taken);
        int shift = instableShift(taken);

        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        List<RuneStatType> pool = new ArrayList<>(Arrays.asList(RuneStatType.values()));
        Collections.shuffle(pool, RNG);

        int applied = 0;
        for (RuneStatType t : pool) {
            if (applied >= cap) break;
            if (!canApplyStatTo(taken, t)) continue;
            float base = rollBaseStat(t, false, shift);
            map.put(t, base * curseMult);
            RuneSlots.tryConsumeSlot(taken);
            applied++;
        }

        if (!map.isEmpty()) {
            RuneStats.set(taken, new RuneStats(map));
            taken.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        updateGlintAfter(taken);
        return true;
    }

    private static final class ExtractionTarget {
        final RuneStatType stat;
        final Holder<Enchantment> enchant;
        final int level;

        ExtractionTarget(RuneStatType stat, Holder<Enchantment> enchant, int level) {
            this.stat = stat;
            this.enchant = enchant;
            this.level = level;
        }

        boolean isStat() {
            return stat != null;
        }

        boolean isEnchant() {
            return enchant != null && level > 0;
        }
    }

    private ExtractionTarget selectExtractionTarget(ItemStack stack) {
        ItemEnchantments ex = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!ex.isEmpty()) {
            for (Object2IntMap.Entry<Holder<Enchantment>> e : ex.entrySet()) {
                if (e.getIntValue() > 0) return new ExtractionTarget(null, e.getKey(), e.getIntValue());
            }
        }

        RuneStats st = RuneStats.get(stack);
        if (st != null && !st.isEmpty()) {
            for (RuneStatType t : RuneStatType.values()) {
                float v = st.get(t);
                if (v != 0.0F) return new ExtractionTarget(t, null, 0);
            }
        }

        return null;
    }

    private void applyExtraction(ItemStack taken) {
        if (GearAttributes.getLevel(taken, GearAttribute.SEALED) >= MAX_ATTR_LEVEL) return;

        ExtractionTarget t = selectExtractionTarget(taken);
        if (t == null) return;

        if (t.isEnchant()) {
            ItemEnchantments ex = taken.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (Object2IntMap.Entry<Holder<Enchantment>> e : ex.entrySet()) {
                if (e.getKey().equals(t.enchant)) continue;
                if (e.getIntValue() > 0) mut.set(e.getKey(), e.getIntValue());
            }
            ItemEnchantments out = mut.toImmutable();
            if (out.isEmpty()) taken.remove(DataComponents.ENCHANTMENTS);
            else taken.set(DataComponents.ENCHANTMENTS, out);
        } else if (t.isStat()) {
            RuneStats st = RuneStats.get(taken);
            if (st != null && !st.isEmpty()) {
                EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
                map.putAll(st.view());
                map.remove(t.stat);
                RuneStats.set(taken, map.isEmpty() ? RuneStats.empty() : new RuneStats(map));
            }
        }

        GearAttributes.addLevel(taken, GearAttribute.SEALED, 1);
        updateGlintAfter(taken);
    }

    private ItemStack createExtractionEtching(ItemStack gear) {
        ExtractionTarget t = selectExtractionTarget(gear);
        if (t == null) return ItemStack.EMPTY;

        ItemStack out = new ItemStack(ModItems.ETCHING.get());

        if (t.isEnchant()) {
            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mut.set(t.enchant, t.level);
            out.set(DataComponents.STORED_ENCHANTMENTS, mut.toImmutable());
            return out;
        }

        if (t.isStat()) {
            EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
            map.put(t.stat, 1.0F);
            RuneStats.set(out, new RuneStats(map));
            return out;
        }

        return ItemStack.EMPTY;
    }

    private void applyRuneOnTake(ItemStack taken, ItemStack enhancement) {
        if (GearAttributes.has(taken, GearAttribute.SEALED)) return;

        if (enhancement.is(ModItems.REPAIR_INSCRIPTION.get())) {
            if (!canApplyRepair(taken)) return;
            if (!reduceMaxDurability(taken, 0.05D)) return;
            taken.set(DataComponents.DAMAGE, 0);
            updateGlintAfter(taken);
            return;
        }

        if (enhancement.is(ModItems.EXPANSION_INSCRIPTION.get())) {
            if (!canApplyExpansion(taken)) return;
            if (!reduceMaxDurability(taken, 0.20D)) return;
            applyExpansion(taken);
            updateGlintAfter(taken);
            return;
        }

        if (enhancement.is(ModItems.NULLIFICATION_INSCRIPTION.get())) {
            if (!canApplyNullification(taken)) return;
            if (GearAttributes.getLevel(taken, GearAttribute.NEGATIVE) >= MAX_ATTR_LEVEL) return;

            applyNullification(taken);
            GearAttributes.addLevel(taken, GearAttribute.NEGATIVE, 1);
            updateGlintAfter(taken);
            return;
        }

        if (enhancement.is(ModItems.UPGRADE_INSCRIPTION.get())) {
            if (applyUpgrade(taken)) updateGlintAfter(taken);
            return;
        }

        if (enhancement.is(ModItems.REROLL_INSCRIPTION.get())) {
            applyReroll(taken);
            return;
        }

        if (enhancement.is(ModItems.CURSED_INSCRIPTION.get())) {
            applyCursedInscription(taken);
            return;
        }

        if (enhancement.is(ModItems.WILD_INSCRIPTION.get())) {
            applyWild(taken);
            return;
        }

        if (enhancement.is(ModItems.EXTRACTION_INSCRIPTION.get())) {
            applyExtraction(taken);
            return;
        }

        if (!isEnhancementItem(enhancement)) return;
        if (effectiveRemaining(taken) <= 0) return;

        RuneStats templateStats = RuneStats.get(enhancement);
        RuneStatType stat = (templateStats != null && !templateStats.isEmpty()) ? getEnhancementStatType(enhancement) : null;

        ItemEnchantments enchants = getEnhancementEnchantments(enhancement);
        boolean hasEnch = enchants != null && !enchants.isEmpty();
        boolean hasStats = stat != null;

        boolean applied = false;

        if (hasStats && canApplyStatTo(taken, stat)) {
            RuneStats base = RuneStats.get(taken);
            RuneStats rolled = RuneStats.rollForApplication(templateStats, this.level.random, isEtching(enhancement));

            boolean changedStats = false;
            if (rolled != null && !rolled.isEmpty()) {
                for (Map.Entry<RuneStatType, Float> e : rolled.view().entrySet()) {
                    if (e.getValue() != 0.0F && (base == null || !base.has(e.getKey()))) {
                        changedStats = true;
                        break;
                    }
                }
            }

            if (changedStats) {
                int shift = instableShift(taken);
                float curseMult = GearAttributes.cursedMultiplier(taken);
                EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);

                for (Map.Entry<RuneStatType, Float> e : rolled.view().entrySet()) {
                    RuneStatType t = e.getKey();
                    float v = e.getValue();
                    if (v == 0.0F) continue;

                    int max = isEtching(enhancement) ? t.etchingMaxPercent() : t.maxPercent();
                    int maxAdj = Math.max(0, max - shift);
                    if (v > maxAdj) v = maxAdj;

                    map.put(t, v * curseMult);
                }

                RuneStats adjusted = map.isEmpty() ? RuneStats.empty() : new RuneStats(map);
                RuneStats.set(taken, RuneStats.combine(base, adjusted));
                taken.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                applied = true;
            }
        }

        if (hasEnch) {
            applied = applyEffectEnchant(taken, enchants) || applied;
        }

        if (applied) {
            RuneSlots.tryConsumeSlot(taken);
        }
    }

    private boolean applyEffectEnchant(ItemStack target, ItemEnchantments enchants) {
        ItemEnchantments current = target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(current);

        boolean changed = false;

        for (var entry : enchants.entrySet()) {
            Holder<Enchantment> h = entry.getKey();
            if (!RuneItem.isEffectEnchantment(h)) continue;

            int desired = RuneItem.clampEffectLevel(h, entry.getIntValue());
            int have = mut.getLevel(h);
            if (have >= desired) continue;

            if (have == 0) {
                List<Holder<Enchantment>> existing = new ArrayList<>(mut.keySet());
                if (!EnchantmentHelper.isEnchantmentCompatible(existing, h)) continue;
            }

            mut.set(h, desired);
            changed = true;
        }

        if (changed) {
            target.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
        }

        return changed;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((lvl, pos) -> this.clearContainer(player, input));
        preview.removeItemNoUpdate(0);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.ARTISANS_WORKBENCH.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        int enhIdx = 0;
        int gearIdx = 1;
        int previewIdx = 2;

        int invStart = 3;
        int invEnd = invStart + 36;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == previewIdx) return ItemStack.EMPTY;

        if (index >= invStart) {
            if (stack.getItem() instanceof RuneItem
                    || stack.getItem() instanceof EtchingItem
                    || stack.is(ModItems.REPAIR_INSCRIPTION.get())
                    || stack.is(ModItems.EXPANSION_INSCRIPTION.get())
                    || stack.is(ModItems.NULLIFICATION_INSCRIPTION.get())
                    || stack.is(ModItems.UPGRADE_INSCRIPTION.get())
                    || stack.is(ModItems.REROLL_INSCRIPTION.get())
                    || stack.is(ModItems.CURSED_INSCRIPTION.get())
                    || stack.is(ModItems.WILD_INSCRIPTION.get())
                    || stack.is(ModItems.EXTRACTION_INSCRIPTION.get())) {
                if (!this.moveItemStackTo(stack, enhIdx, enhIdx + 1, false)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(stack, gearIdx, gearIdx + 1, false)) return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stack, invStart, invEnd, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }
}
