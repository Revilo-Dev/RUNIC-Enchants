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
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.screen.ModMenuTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EtchingTableMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Level level;
    private final SimpleContainer input = new SimpleContainer(2) {
        @Override public void setChanged() { super.setChanged(); EtchingTableMenu.this.slotsChanged(this); }
        @Override public int getMaxStackSize() { return 1; }
    };
    private final ResultContainer result = new ResultContainer();
    private static final Random RANDOM = new Random();

    private EtchingTableMenu(int id, Inventory playerInv, ContainerLevelAccess access) {
        super(ModMenuTypes.ETCHING_TABLE.get(), id);
        this.access = access;
        this.level = playerInv.player.level();

        this.addSlot(new Slot(input, 0, 8, 50));
        this.addSlot(new Slot(input, 1, 44, 50));

        this.addSlot(new Slot(result, 0, 98, 50) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return !this.getItem().isEmpty(); }

            @Override
            public void onTake(Player player, ItemStack taken) {
                ItemStack rune = input.getItem(1);

                if (rune.is(ModItems.REPAIR_RUNE.get())) {
                    int cap = RuneSlots.capacity(taken);
                    if (cap > 1) {
                        RuneSlots.removeOneSlot(taken);
                    }
                } else {
                    RuneSlots.tryConsumeSlot(taken);
                }

                consumeInputs();
                EtchingTableMenu.this.access.execute((lvl, pos) ->
                        lvl.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F)
                );

                result.setItem(0, ItemStack.EMPTY);
                EtchingTableMenu.this.updateResult();
                super.onTake(player, taken);
            }
        });

        final int invX = 8, invY = 84;
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                this.addSlot(new Slot(playerInv, c + r * 9 + 9, invX + c * 18, invY + r * 18));
        final int hotbarY = invY + 3 * 18 + 4;
        for (int c = 0; c < 9; c++)
            this.addSlot(new Slot(playerInv, c, invX + c * 18, hotbarY));

        updateResult();
    }

    public static EtchingTableMenu server(int id, Inventory inv, Level level, BlockPos pos) {
        return new EtchingTableMenu(id, inv, ContainerLevelAccess.create(level, pos));
    }

    public static EtchingTableMenu client(int id, Inventory inv, BlockPos pos) {
        return new EtchingTableMenu(id, inv, ContainerLevelAccess.create(inv.player.level(), pos));
    }

    @Override
    public void slotsChanged(Container container) { updateResult(); }

    private void updateResult() {
        ItemStack target = input.getItem(0);
        ItemStack rune = input.getItem(1);
        ItemStack out = computeResult(target, rune);
        result.setItem(0, out);
        broadcastChanges();
    }

    private void consumeInputs() {
        ItemStack t = input.getItem(0);
        ItemStack r = input.getItem(1);
        if (!t.isEmpty()) t.shrink(1);
        if (!r.isEmpty()) r.shrink(1);
        input.setChanged();
    }

    private ItemStack computeResult(ItemStack target, ItemStack rune) {
        if (target.isEmpty() || rune.isEmpty()) return ItemStack.EMPTY;

        if (rune.is(ModItems.REPAIR_RUNE.get())) {
            int cap = RuneSlots.capacity(target);
            if (cap <= 1) return ItemStack.EMPTY;
            return applyRepairRune(target);
        }

        if (!rune.is(ModItems.ENHANCED_RUNE.get())) return ItemStack.EMPTY;
        if (RuneSlots.remaining(target) <= 0) return ItemStack.EMPTY;

        Holder<Enchantment> ench = null;
        int lvlVal = 0;

        ItemEnchantments stored = rune.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = rune.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (!stored.isEmpty()) {
            for (Object2IntMap.Entry<Holder<Enchantment>> e : stored.entrySet()) {
                ench = e.getKey();
                lvlVal = e.getIntValue();
                break;
            }
        } else if (!direct.isEmpty()) {
            for (Object2IntMap.Entry<Holder<Enchantment>> e : direct.entrySet()) {
                ench = e.getKey();
                lvlVal = e.getIntValue();
                break;
            }
        }

        if (ench == null || lvlVal <= 0) return ItemStack.EMPTY;

        ItemStack out = target.copy();
        if (!isCompatibleWithExisting(out, ench)) return ItemStack.EMPTY;
        if (!canApplyToTarget(out, ench)) return ItemStack.EMPTY;

        int max = ench.value().getMaxLevel();
        int applyLevel = Math.min(lvlVal, max);

        ItemEnchantments existing = out.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(existing);
        mut.set(ench, applyLevel);
        out.set(DataComponents.ENCHANTMENTS, mut.toImmutable());

        return out;
    }

    private ItemStack applyRepairRune(ItemStack target) {
        int max = target.getMaxDamage();
        if (max <= 0) return ItemStack.EMPTY;

        int cap = RuneSlots.capacity(target);
        if (cap <= 1) return ItemStack.EMPTY;

        ItemStack out = target.copy();

        ItemEnchantments existing = out.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!existing.isEmpty()) {
            List<Holder<Enchantment>> list = new ArrayList<>(existing.keySet());
            if (!list.isEmpty()) {
                Holder<Enchantment> toRemove = list.get(RANDOM.nextInt(list.size()));
                ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(existing);
                mut.set(toRemove, 0); // Remove by setting level 0
                out.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
            }
        }

        RuneSlots.removeOneSlot(out);

        int damage = out.getOrDefault(DataComponents.DAMAGE, 0);
        int heal = Math.max(1, (int) Math.floor(max * 0.25f));
        int newDamage = Math.max(0, damage - heal);
        out.set(DataComponents.DAMAGE, newDamage);

        return out;
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
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack originalCopy = stackInSlot.copy();

        final int RESULT_IDX = 2;
        final int INV_START = 3;
        final int INV_END = INV_START + 27;
        final int HOTBAR_START = INV_END;
        final int HOTBAR_END = HOTBAR_START + 9;

        if (index == RESULT_IDX) {
            ItemStack rune = input.getItem(1);
            if (rune.is(ModItems.REPAIR_RUNE.get())) {
                int cap = RuneSlots.capacity(stackInSlot);
                if (cap > 1) {
                    RuneSlots.removeOneSlot(stackInSlot);
                }
            } else {
                if (!RuneSlots.tryConsumeSlot(stackInSlot)) return ItemStack.EMPTY;
            }

            consumeInputs();
            this.access.execute((lvl, pos) ->
                    lvl.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F)
            );

            if (!this.moveItemStackTo(stackInSlot, INV_START, HOTBAR_END, true)) return ItemStack.EMPTY;
            slot.set(ItemStack.EMPTY);
            result.setItem(0, ItemStack.EMPTY);
            this.updateResult();
            return originalCopy;
        }

        if (index < INV_START) {
            if (!this.moveItemStackTo(stackInSlot, INV_START, HOTBAR_END, false)) return ItemStack.EMPTY;
        } else {
            if (stackInSlot.is(ModItems.ENHANCED_RUNE.get()) || stackInSlot.is(ModItems.REPAIR_RUNE.get())) {
                if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (stackInSlot.getCount() == originalCopy.getCount()) return ItemStack.EMPTY;
        return originalCopy;
    }

    private static boolean canApplyToTarget(ItemStack target, Holder<Enchantment> newEnch) {
        return newEnch.value().canEnchant(target);
    }

    private static boolean isCompatibleWithExisting(ItemStack stack, Holder<Enchantment> newEnch) {
        ItemEnchantments existing = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> e : existing.entrySet()) {
            if (!Enchantment.areCompatible(e.getKey(), newEnch)) return false;
        }
        return true;
    }
}
