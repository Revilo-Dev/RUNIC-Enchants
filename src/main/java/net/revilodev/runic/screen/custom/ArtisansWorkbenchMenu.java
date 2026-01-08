package net.revilodev.runic.screen.custom;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.revilodev.runic.block.ModBlocks;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.registry.ModDataComponents;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.*;

public class ArtisansWorkbenchMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final Level level;

    private final SimpleContainer input = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            ArtisansWorkbenchMenu.this.slotsChanged(this);
        }
    };

    private final ResultContainer result = new ResultContainer();
    private static final Random RNG = new Random();

    private ArtisansWorkbenchMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(net.revilodev.runic.screen.ModMenuTypes.ARTISANS_WORKBENCH.get(), id);
        this.access = access;
        this.level = inv.player.level();

        this.addSlot(new Slot(input, 0, 8, 50) {
            @Override
            public int getMaxStackSize() { return 1; }
        });

        this.addSlot(new Slot(input, 1, 44, 50) {
            @Override
            public int getMaxStackSize() { return 64; }
        });

        this.addSlot(new Slot(result, 0, 98, 50) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public boolean mayPickup(Player player) {
                return !this.getItem().isEmpty();
            }

            @Override
            public void onTake(Player player, ItemStack taken) {
                ItemStack rune = input.getItem(1).copy();
                applyRuneOnTake(taken, rune);
                consumeInputs();
                playUseSound();
                result.setItem(0, ItemStack.EMPTY);
                ArtisansWorkbenchMenu.this.updateResult();
                super.onTake(player, taken);
            }
        });

        int x = 8;
        int y = 84;

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                this.addSlot(new Slot(inv, c + r * 9 + 9, x + c * 18, y + r * 18));

        for (int c = 0; c < 9; c++)
            this.addSlot(new Slot(inv, c, x + c * 18, y + 58));

        updateResult();
    }

    public static ArtisansWorkbenchMenu server(int id, Inventory inv, Level level, BlockPos pos) {
        return new ArtisansWorkbenchMenu(id, inv, ContainerLevelAccess.create(level, pos));
    }

    public static ArtisansWorkbenchMenu client(int id, Inventory inv, BlockPos pos) {
        return new ArtisansWorkbenchMenu(id, inv, ContainerLevelAccess.create(inv.player.level(), pos));
    }

    @Override
    public void slotsChanged(Container container) {
        updateResult();
    }

    private void updateResult() {
        ItemStack target = input.getItem(0);
        ItemStack rune = input.getItem(1);
        result.setItem(0, preview(target, rune));
        broadcastChanges();
    }

    private void consumeInputs() {
        if (!input.getItem(0).isEmpty()) input.getItem(0).shrink(1);

        ItemStack runeIn = input.getItem(1);
        if (!runeIn.isEmpty() && !runeIn.is(ModItems.REPAIR_INSCRIPTION.get())) {
            runeIn.shrink(1);
        }

        input.setChanged();
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

    private RuneStatType getEnhancementStatType(ItemStack enhancement) {
        RuneStats stats = RuneStats.get(enhancement);
        if (stats == null || stats.isEmpty()) return null;
        for (RuneStatType t : stats.view().keySet()) return t;
        return null;
    }

    private boolean canApplyStatTo(ItemStack target, RuneStatType stat) {
        if (stat == null) return false;
        if (RuneStats.get(target).has(stat)) return false;

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

    private boolean hasUpgradeableStat(ItemStack target, RuneStats current) {
        for (Map.Entry<RuneStatType, Float> e : current.view().entrySet()) {
            RuneStatType t = e.getKey();
            float v = e.getValue();
            if (v <= 0.0F) continue;

            float cap = t.cap();
            if (cap > 0.0F && v >= cap - 0.0001F) continue;

            return true;
        }
        return false;
    }

    private boolean hasUpgradeableEnchant(ItemStack target) {
        ItemEnchantments ex = target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (ex.isEmpty()) return false;

        for (Object2IntMap.Entry<Holder<Enchantment>> e : ex.entrySet()) {
            if (RuneItem.isEffectEnchantment(e.getKey())) continue;
            if (e.getIntValue() < e.getKey().value().getMaxLevel()) return true;
        }
        return false;
    }

    private boolean canApplyNullification(ItemStack target) {
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
        RuneStats st = RuneStats.get(target);
        if (st != null && !st.isEmpty() && hasUpgradeableStat(target, st)) return true;
        return hasUpgradeableEnchant(target);
    }

    private ItemStack preview(ItemStack target, ItemStack enhancement) {
        if (target.isEmpty() || enhancement.isEmpty()) return ItemStack.EMPTY;

        if (enhancement.is(ModItems.REPAIR_INSCRIPTION.get())) {
            if (RuneSlots.capacity(target) <= 1) return ItemStack.EMPTY;
            return target.copy();
        }

        if (enhancement.is(ModItems.EXPANSION_INSCRIPTION.get())) {
            if (RuneSlots.expansionsUsed(target) >= 3) return ItemStack.EMPTY;
            return target.copy();
        }

        if (enhancement.is(ModItems.NULLIFICATION_INSCRIPTION.get())) {
            return canApplyNullification(target) ? target.copy() : ItemStack.EMPTY;
        }

        if (enhancement.is(ModItems.UPGRADE_INSCRIPTION.get())) {
            return canApplyUpgrade(target) ? target.copy() : ItemStack.EMPTY;
        }

        if (!isEnhancementItem(enhancement)) return ItemStack.EMPTY;
        if (RuneSlots.remaining(target) <= 0) return ItemStack.EMPTY;

        RuneStats stats = RuneStats.get(enhancement);
        RuneStatType stat = (stats != null && !stats.isEmpty()) ? getEnhancementStatType(enhancement) : null;
        boolean statApplicable = stat != null && canApplyStatTo(target, stat);

        ItemEnchantments enchants = getEnhancementEnchantments(enhancement);
        boolean effectApplicable = canApplyAnyEffectEnchant(target, enchants);

        if (!statApplicable && !effectApplicable) return ItemStack.EMPTY;

        return target.copy();
    }

    private RuneStatType pickRandomUpgradeableStat(RuneStats current) {
        List<RuneStatType> options = new ArrayList<>();
        for (Map.Entry<RuneStatType, Float> e : current.view().entrySet()) {
            RuneStatType t = e.getKey();
            float v = e.getValue();
            if (v <= 0.0F) continue;

            float cap = t.cap();
            if (cap > 0.0F && v >= cap - 0.0001F) continue;

            options.add(t);
        }
        if (options.isEmpty()) return null;
        return options.get(RNG.nextInt(options.size()));
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

    private boolean applyUpgrade(ItemStack taken) {
        boolean changed = false;

        RuneStats current = RuneStats.get(taken);
        if (current != null && !current.isEmpty() && hasUpgradeableStat(taken, current)) {
            RuneStatType t = pickRandomUpgradeableStat(current);
            if (t != null) {
                EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
                map.putAll(current.view());

                float v = map.getOrDefault(t, 0.0F);
                float upgraded = v * 1.15f;

                float cap = t.cap();
                if (cap > 0.0F && upgraded > cap) upgraded = cap;

                map.put(t, upgraded);

                RuneStats.set(taken, new RuneStats(map));
                changed = true;
            }
        } else {
            ItemEnchantments ex = taken.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (ex.isEmpty()) return false;

            List<Holder<Enchantment>> up = new ArrayList<>();
            for (Object2IntMap.Entry<Holder<Enchantment>> e : ex.entrySet()) {
                if (RuneItem.isEffectEnchantment(e.getKey())) continue;
                if (e.getIntValue() < e.getKey().value().getMaxLevel()) up.add(e.getKey());
            }
            if (up.isEmpty()) return false;

            Holder<Enchantment> chosen = up.get(RNG.nextInt(up.size()));
            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ex);
            mut.set(chosen, ex.getLevel(chosen) + 1);
            taken.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
            changed = true;
        }

        updateGlintAfter(taken);
        return changed;
    }

    private void applyRuneOnTake(ItemStack taken, ItemStack enhancement) {
        if (enhancement.is(ModItems.REPAIR_INSCRIPTION.get())) {
            int cap = RuneSlots.capacity(taken);
            if (cap > 1) {
                RuneSlots.removeOneSlot(taken);
                taken.set(DataComponents.DAMAGE, 0);
                updateGlintAfter(taken);
            }
            return;
        }

        if (enhancement.is(ModItems.EXPANSION_INSCRIPTION.get())) {
            applyExpansion(taken);
            return;
        }

        if (enhancement.is(ModItems.NULLIFICATION_INSCRIPTION.get())) {
            applyNullification(taken);
            return;
        }

        if (enhancement.is(ModItems.UPGRADE_INSCRIPTION.get())) {
            if (applyUpgrade(taken)) {
                RuneSlots.tryConsumeSlot(taken);
            }
            return;
        }

        if (!isEnhancementItem(enhancement)) return;

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
                RuneStats.set(taken, RuneStats.combine(base, rolled));
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
        result.removeItemNoUpdate(0);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.ARTISANS_WORKBENCH.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        int resultIdx = 2;
        int invStart = 3;
        int invEnd = invStart + 36;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == resultIdx) {
            ItemStack enhancement = input.getItem(1).copy();
            ItemStack taken = stack.copy();
            result.setItem(0, ItemStack.EMPTY);
            slot.set(ItemStack.EMPTY);

            applyRuneOnTake(taken, enhancement);
            consumeInputs();
            playUseSound();
            this.updateResult();

            if (!this.moveItemStackTo(taken, invStart, invEnd, true)) return ItemStack.EMPTY;
            return copy;
        }

        if (index < invStart) {
            if (!this.moveItemStackTo(stack, invStart, invEnd, false)) return ItemStack.EMPTY;
        } else {
            if (stack.getItem() instanceof RuneItem
                    || stack.getItem() instanceof EtchingItem
                    || stack.is(ModItems.REPAIR_INSCRIPTION.get())
                    || stack.is(ModItems.EXPANSION_INSCRIPTION.get())
                    || stack.is(ModItems.NULLIFICATION_INSCRIPTION.get())
                    || stack.is(ModItems.UPGRADE_INSCRIPTION.get())) {
                if (!this.moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }
}
