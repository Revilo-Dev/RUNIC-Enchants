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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.revilodev.runic.block.ModBlocks;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.screen.ModMenuTypes;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

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
    private static final Random RANDOM = new Random();

    private EtchingTableMenu(int id, Inventory playerInv, ContainerLevelAccess access) {
        super(ModMenuTypes.ETCHING_TABLE.get(), id);
        this.access = access;
        this.level = playerInv.player.level();

        this.addSlot(new Slot(input, 0, 8, 50) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(input, 1, 44, 50) {
            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        this.addSlot(new Slot(result, 0, 98, 50) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return !this.getItem().isEmpty();
            }

            @Override
            public void onTake(Player player, ItemStack taken) {
                ItemStack rune = input.getItem(1).copy();
                applyRuneEffectOnTaken(taken, rune);
                consumeInputs();
                playUseSound();
                result.setItem(0, ItemStack.EMPTY);
                EtchingTableMenu.this.updateResult();
                super.onTake(player, taken);
            }
        });

        int invX = 8;
        int invY = 84;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                this.addSlot(new Slot(playerInv, c + r * 9 + 9, invX + c * 18, invY + r * 18));
            }
        }
        int hotbarY = invY + 3 * 18 + 4;
        for (int c = 0; c < 9; c++) {
            this.addSlot(new Slot(playerInv, c, invX + c * 18, hotbarY));
        }

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
        ItemStack rune = input.getItem(1);
        result.setItem(0, previewResult(target, rune));
        broadcastChanges();
    }

    private void consumeInputs() {
        ItemStack t = input.getItem(0);
        ItemStack r = input.getItem(1);
        if (!t.isEmpty()) {
            t.shrink(1);
        }
        if (!r.isEmpty()) {
            r.shrink(1);
        }
        input.setChanged();
    }

    private void playUseSound() {
        this.access.execute((lvl, pos) ->
                lvl.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F)
        );
    }

    private ItemStack previewResult(ItemStack target, ItemStack rune) {
        if (target.isEmpty() || rune.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (rune.is(ModItems.REPAIR_RUNE.get())) {
            if (RuneSlots.capacity(target) <= 1) {
                return ItemStack.EMPTY;
            }
            return target.copy();
        }

        if (rune.is(ModItems.EXPANSION_RUNE.get())) {
            if (RuneSlots.expansionsUsed(target) >= 3) {
                return ItemStack.EMPTY;
            }
            return target.copy();
        }

        if (rune.is(ModItems.NULLIFICATION_RUNE.get())) {
            ItemEnchantments ex = target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (ex.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return target.copy();
        }

        if (rune.is(ModItems.UPGRADE_RUNE.get())) {
            ItemEnchantments ex = target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (ex.isEmpty()) {
                return ItemStack.EMPTY;
            }
            List<Holder<Enchantment>> upgradable = new ArrayList<>();
            for (Object2IntMap.Entry<Holder<Enchantment>> e : ex.entrySet()) {
                if (e.getIntValue() < e.getKey().value().getMaxLevel()) {
                    upgradable.add(e.getKey());
                }
            }
            if (upgradable.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return target.copy();
        }

        if (!rune.is(ModItems.ENHANCED_RUNE.get())) {
            return ItemStack.EMPTY;
        }

        if (RuneSlots.remaining(target) <= 0) {
            return ItemStack.EMPTY;
        }

        RuneStats runeStats = RuneItem.getRuneStats(rune);
        boolean hasStats = !runeStats.isEmpty();
        Holder<Enchantment> effect = RuneItem.getPrimaryEffectEnchantment(rune);
        boolean hasEffect = effect != null && RuneItem.isEffectEnchantment(effect);

        if (!hasStats && !hasEffect) {
            return ItemStack.EMPTY;
        }

        ItemStack out = target.copy();

        if (hasStats) {
            RuneStats baseStats = RuneStats.get(out);
            RuneStats combined = combineStats(baseStats, runeStats);
            RuneStats.set(out, combined);
        }

        if (hasEffect) {
            if (!effect.value().canEnchant(out)) {
                return ItemStack.EMPTY;
            }
            ItemEnchantments existing = out.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(existing);
            mut.set(effect, 1);
            out.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
        }

        return out;
    }

    private void applyRuneEffectOnTaken(ItemStack taken, ItemStack rune) {
        if (rune.is(ModItems.REPAIR_RUNE.get())) {
            int cap = RuneSlots.capacity(taken);
            if (cap > 1) {
                RuneSlots.removeOneSlot(taken);
                ItemEnchantments existing = taken.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                if (!existing.isEmpty()) {
                    List<Holder<Enchantment>> keys = new ArrayList<>(existing.keySet());
                    if (!keys.isEmpty()) {
                        Holder<Enchantment> remove = keys.get(RANDOM.nextInt(keys.size()));
                        ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(existing);
                        mut.set(remove, 0);
                        taken.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
                    }
                }
                int max = taken.getMaxDamage();
                int damage = taken.getOrDefault(DataComponents.DAMAGE, 0);
                int heal = Math.max(1, (int)Math.floor(max * 0.25f));
                taken.set(DataComponents.DAMAGE, Math.max(0, damage - heal));
            }
            return;
        }

        if (rune.is(ModItems.EXPANSION_RUNE.get())) {
            int used = RuneSlots.expansionsUsed(taken);
            if (used < 3) {
                int currentMax = taken.getMaxDamage();
                int newMax = (int)Math.floor(currentMax * 0.75f);
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
            ItemEnchantments existing = taken.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (!existing.isEmpty()) {
                List<Holder<Enchantment>> keys = new ArrayList<>(existing.keySet());
                if (!keys.isEmpty()) {
                    Holder<Enchantment> remove = keys.get(RANDOM.nextInt(keys.size()));
                    ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(existing);
                    mut.set(remove, 0);
                    taken.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
                }
            }
            return;
        }

        if (rune.is(ModItems.UPGRADE_RUNE.get())) {
            ItemEnchantments existing = taken.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (existing.isEmpty()) return;

            List<Holder<Enchantment>> upgradable = new ArrayList<>();
            for (Object2IntMap.Entry<Holder<Enchantment>> e : existing.entrySet()) {
                if (e.getIntValue() < e.getKey().value().getMaxLevel()) upgradable.add(e.getKey());
            }
            if (upgradable.isEmpty()) return;

            Holder<Enchantment> chosen = upgradable.get(RANDOM.nextInt(upgradable.size()));
            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(existing);
            int current = existing.getLevel(chosen);
            mut.set(chosen, current + 1);
            taken.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
            return;
        }

        if (rune.is(ModItems.ENHANCED_RUNE.get())) {
            ItemEnchantments storedRune = rune.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            ItemEnchantments directRune = rune.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            ItemEnchantments runeEnchs = !storedRune.isEmpty() ? storedRune : directRune;

            RuneStats runeStats = net.revilodev.runic.item.custom.RuneItem.getRuneStats(rune);
            boolean hasStats = runeStats != null && !runeStats.isEmpty();

            if (hasStats && runeEnchs.isEmpty()) {
                RuneStats base = RuneStats.get(taken);
                RuneStats rolled = RuneStats.rollForApplication(runeStats, this.level.random);
                RuneStats combined = RuneStats.combine(base, rolled);
                RuneStats.set(taken, combined);
                RuneSlots.tryConsumeSlot(taken);
                return;
            }

            if (!runeEnchs.isEmpty()) {
                RuneSlots.tryConsumeSlot(taken);
            }
        }
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

    public ItemStack getPreviewStack() {
        return input.getItem(0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        int resultIndex = 2;
        int invStart = 3;
        int hotbarEnd = invStart + 36;

        ItemStack stackInSlot = slot.getItem();
        ItemStack originalCopy = stackInSlot.copy();

        if (index == resultIndex) {
            ItemStack rune = input.getItem(1).copy();
            ItemStack taken = stackInSlot.copy();

            result.setItem(0, ItemStack.EMPTY);
            slot.set(ItemStack.EMPTY);

            applyRuneEffectOnTaken(taken, rune);
            consumeInputs();
            playUseSound();
            this.updateResult();

            if (!this.moveItemStackTo(taken, invStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
            return originalCopy;
        }

        if (index < invStart) {
            if (!this.moveItemStackTo(stackInSlot, invStart, hotbarEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (stackInSlot.is(ModItems.ENHANCED_RUNE.get()) ||
                    stackInSlot.is(ModItems.REPAIR_RUNE.get()) ||
                    stackInSlot.is(ModItems.EXPANSION_RUNE.get()) ||
                    stackInSlot.is(ModItems.NULLIFICATION_RUNE.get()) ||
                    stackInSlot.is(ModItems.UPGRADE_RUNE.get())) {
                if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return originalCopy;
    }

    private static RuneStats combineStats(RuneStats base, RuneStats add) {
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        for (RuneStatType type : RuneStatType.values()) {
            float v = base.get(type) + add.get(type);
            if (v != 0.0F) {
                map.put(type, v);
            }
        }
        return new RuneStats(map);
    }
}
