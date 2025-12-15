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
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.*;

public class EtchingTableMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final Level level;

    private final SimpleContainer input = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            EtchingTableMenu.this.slotsChanged(this);
        }
    };

    private final ResultContainer result = new ResultContainer();
    private static final Random RNG = new Random();

    private EtchingTableMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(net.revilodev.runic.screen.ModMenuTypes.ETCHING_TABLE.get(), id);
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
                EtchingTableMenu.this.updateResult();
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

    public static EtchingTableMenu server(int id, Inventory inv, Level level, BlockPos pos) {
        return new EtchingTableMenu(id, inv, ContainerLevelAccess.create(level, pos));
    }

    public static EtchingTableMenu client(int id, Inventory inv, BlockPos pos) {
        return new EtchingTableMenu(id, inv, ContainerLevelAccess.create(inv.player.level(), pos));
    }

    @Override
    public void slotsChanged(Container container) {
        updateResult();
    }

    private void updateResult() {
        ItemStack target = input.getItem(0);
        ItemStack rune   = input.getItem(1);
        result.setItem(0, preview(target, rune));
        broadcastChanges();
    }

    private void consumeInputs() {
        if (!input.getItem(0).isEmpty()) input.getItem(0).shrink(1);
        if (!input.getItem(1).isEmpty()) input.getItem(1).shrink(1);
        input.setChanged();
    }

    private void playUseSound() {
        this.access.execute((lvl, pos) ->
                lvl.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1f, 1f)
        );
    }

    private boolean canApplyRuneTo(ItemStack target, ItemStack rune) {
        if (!rune.is(ModItems.ENHANCED_RUNE.get())) return false;

        RuneStatType stat = getRuneStatType(rune);
        Item item = target.getItem();

        if (stat != null) {
            float cap = stat.cap();
            if (cap > 0.0F) {
                float current = RuneStats.get(target).get(stat);
                if (current >= cap - 0.0001F) {
                    return false;
                }
            }

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
                     SWIMMING_SPEED, WATER_BREATHING, FALL_REDUCTION,
                     TOUGHNESS, HEALING_EFFICIENCY, JUMP_HEIGHT ->
                        item instanceof ArmorItem;

                case DURABILITY ->
                        target.isDamageableItem();

                default -> true;
            };
        }

        return true;
    }

    private RuneStatType getRuneStatType(ItemStack rune) {
        RuneStats stats = RuneItem.getRuneStats(rune);
        if (stats == null || stats.isEmpty()) return null;

        for (RuneStatType t : stats.view().keySet()) {
            return t;
        }
        return null;
    }

    private boolean canApplyEffectEnchant(ItemStack target, Holder<Enchantment> effect) {
        if (effect == null) return false;
        if (!RuneItem.isEffectEnchantment(effect)) return false;

        ItemEnchantments ex = target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (ex.getLevel(effect) > 0) return false;

        List<Holder<Enchantment>> existing = new ArrayList<>(ex.keySet());
        return EnchantmentHelper.isEnchantmentCompatible(existing, effect);
    }

    private ItemStack preview(ItemStack target, ItemStack rune) {
        if (target.isEmpty() || rune.isEmpty()) return ItemStack.EMPTY;

        if (rune.is(ModItems.REPAIR_RUNE.get())) {
            if (RuneSlots.capacity(target) <= 1) return ItemStack.EMPTY;
            return target.copy();
        }

        if (rune.is(ModItems.EXPANSION_RUNE.get())) {
            if (RuneSlots.expansionsUsed(target) >= 3) return ItemStack.EMPTY;
            return target.copy();
        }

        if (rune.is(ModItems.NULLIFICATION_RUNE.get())) {
            ItemEnchantments ex = target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            return ex.isEmpty() ? ItemStack.EMPTY : target.copy();
        }

        if (rune.is(ModItems.UPGRADE_RUNE.get())) {
            ItemEnchantments ex = target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (ex.isEmpty()) return ItemStack.EMPTY;
            List<Holder<Enchantment>> list = new ArrayList<>();
            for (Object2IntMap.Entry<Holder<Enchantment>> e : ex.entrySet()) {
                if (RuneItem.isEffectEnchantment(e.getKey())) continue;
                if (e.getIntValue() < e.getKey().value().getMaxLevel()) list.add(e.getKey());
            }
            return list.isEmpty() ? ItemStack.EMPTY : target.copy();
        }

        if (!rune.is(ModItems.ENHANCED_RUNE.get())) return ItemStack.EMPTY;
        if (RuneSlots.remaining(target) <= 0) return ItemStack.EMPTY;
        if (!canApplyRuneTo(target, rune)) return ItemStack.EMPTY;

        RuneStats stats = RuneItem.getRuneStats(rune);
        Holder<Enchantment> effect = RuneItem.getPrimaryEffectEnchantment(rune);
        boolean hasStats = stats != null && !stats.isEmpty();
        boolean hasEffect = effect != null && RuneItem.isEffectEnchantment(effect);

        if (!hasStats && !hasEffect) return ItemStack.EMPTY;

        if (hasEffect && !canApplyEffectEnchant(target, effect)) return ItemStack.EMPTY;

        return target.copy();
    }

    private void applyRuneOnTake(ItemStack taken, ItemStack rune) {
        if (rune.is(ModItems.REPAIR_RUNE.get())) {
            int cap = RuneSlots.capacity(taken);
            if (cap > 1) {
                RuneSlots.removeOneSlot(taken);
                ItemEnchantments ex = taken.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                if (!ex.isEmpty()) {
                    List<Holder<Enchantment>> keys = new ArrayList<>(ex.keySet());
                    Holder<Enchantment> remove = keys.get(RNG.nextInt(keys.size()));
                    ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ex);
                    mut.set(remove, 0);
                    taken.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
                }
                int max = taken.getMaxDamage();
                int dmg = taken.getOrDefault(DataComponents.DAMAGE, 0);
                int heal = Math.max(1, (int) Math.floor(max * 0.25f));
                taken.set(DataComponents.DAMAGE, Math.max(0, dmg - heal));
            }
            return;
        }

        if (rune.is(ModItems.EXPANSION_RUNE.get())) {
            int used = RuneSlots.expansionsUsed(taken);
            if (used < 3) {
                int current = taken.getMaxDamage();
                int newMax = (int) Math.floor(current * 0.75f);
                if (newMax >= 1) {
                    int dmg = taken.getOrDefault(DataComponents.DAMAGE, 0);
                    taken.set(DataComponents.DAMAGE, Math.min(newMax - 1, dmg));
                    taken.set(DataComponents.MAX_DAMAGE, newMax);
                    RuneSlots.addOneSlot(taken);
                    RuneSlots.incrementExpansion(taken);
                }
            }
            return;
        }

        if (rune.is(ModItems.NULLIFICATION_RUNE.get())) {
            ItemEnchantments ex = taken.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (!ex.isEmpty()) {
                List<Holder<Enchantment>> keys = new ArrayList<>(ex.keySet());
                Holder<Enchantment> remove = keys.get(RNG.nextInt(keys.size()));
                ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ex);
                mut.set(remove, 0);
                taken.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
            }
            return;
        }

        if (rune.is(ModItems.UPGRADE_RUNE.get())) {
            ItemEnchantments ex = taken.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (ex.isEmpty()) return;

            List<Holder<Enchantment>> up = new ArrayList<>();
            for (Object2IntMap.Entry<Holder<Enchantment>> e : ex.entrySet()) {
                if (RuneItem.isEffectEnchantment(e.getKey())) continue;
                if (e.getIntValue() < e.getKey().value().getMaxLevel()) up.add(e.getKey());
            }
            if (up.isEmpty()) return;

            Holder<Enchantment> chosen = up.get(RNG.nextInt(up.size()));
            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ex);
            mut.set(chosen, ex.getLevel(chosen) + 1);
            taken.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
            return;
        }

        if (!rune.is(ModItems.ENHANCED_RUNE.get())) return;

        RuneStats runeStats = RuneItem.getRuneStats(rune);
        ItemEnchantments stored = rune.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = rune.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments enchants = !stored.isEmpty() ? stored : direct;

        boolean hasEnch = !enchants.isEmpty();
        boolean hasStats = runeStats != null && !runeStats.isEmpty();

        boolean applied = false;

        if (hasStats) {
            RuneStats base = RuneStats.get(taken);
            RuneStats rolled = RuneStats.rollForApplication(runeStats, this.level.random);
            RuneStats.set(taken, RuneStats.combine(base, rolled));
            taken.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            applied = true;
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

            if (current.getLevel(h) > 0) {
                continue;
            }

            List<Holder<Enchantment>> existing = new ArrayList<>(mut.keySet());
            if (!EnchantmentHelper.isEnchantmentCompatible(existing, h)) {
                continue;
            }

            mut.set(h, RuneItem.forcedEffectLevel(h));
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
        return stillValid(this.access, player, ModBlocks.ETCHING_TABLE.get());
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
            ItemStack rune = input.getItem(1).copy();
            ItemStack taken = stack.copy();
            result.setItem(0, ItemStack.EMPTY);
            slot.set(ItemStack.EMPTY);

            applyRuneOnTake(taken, rune);
            consumeInputs();
            playUseSound();
            this.updateResult();

            if (!this.moveItemStackTo(taken, invStart, invEnd, true)) return ItemStack.EMPTY;
            return copy;
        }

        if (index < invStart) {
            if (!this.moveItemStackTo(stack, invStart, invEnd, false)) return ItemStack.EMPTY;
        } else {
            if (stack.is(ModItems.ENHANCED_RUNE.get()) ||
                    stack.is(ModItems.REPAIR_RUNE.get()) ||
                    stack.is(ModItems.EXPANSION_RUNE.get()) ||
                    stack.is(ModItems.NULLIFICATION_RUNE.get()) ||
                    stack.is(ModItems.UPGRADE_RUNE.get())) {
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
