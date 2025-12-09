package net.revilodev.runic.screen.custom;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
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
        super(ModMenuTypes.ETCHING_TABLE.get(), id);
        this.access = access;
        this.level = inv.player.level();

        // Target slot
        this.addSlot(new Slot(input, 0, 8, 50) {
            @Override
            public int getMaxStackSize() { return 1; }
        });

        // Rune slot
        this.addSlot(new Slot(input, 1, 44, 50) {
            @Override
            public int getMaxStackSize() { return 64; }
        });

        // Output slot
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

        // Player inventory
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

    /* ============================================================
       RUNE APPLICATION RULES
       ============================================================ */

    private boolean canApplyRuneTo(ItemStack target, ItemStack rune) {

        // Runes must be enhanced runes
        if (!rune.is(ModItems.ENHANCED_RUNE.get())) return false;

        RuneStats rs = RuneItem.getRuneStats(rune);
        if (rs.isEmpty()) return true; // Effect-only rune — always allowed

        RuneStatType stat = getRuneStatType(rune);
        if (stat == null) return false;

        Item item = target.getItem();

        return switch (stat) {
            case ATTACK_DAMAGE, ATTACK_SPEED, ATTACK_RANGE, SWEEPING_RANGE -> item instanceof SwordItem
                    || item instanceof AxeItem
                    || item instanceof TridentItem;

            case MOVEMENT_SPEED, HEALTH, RESISTANCE, FIRE_RESISTANCE, BLAST_RESISTANCE, PROJECTILE_RESISTANCE,
                 KNOCKBACK_RESISTANCE -> item instanceof ArmorItem;

            case MINING_SPEED, FORTUNE -> item instanceof PickaxeItem
                    || item instanceof ShovelItem
                    || item instanceof AxeItem
                    || item instanceof HoeItem;

            case SWIMMING_SPEED, WATER_BREATHING -> item instanceof ArmorItem;

            case FALL_REDUCTION -> item instanceof ArmorItem;

            case SOUL_SPEED, SWIFT_SNEAK -> item instanceof ArmorItem;

            case DRAW_SPEED -> item instanceof BowItem || item instanceof CrossbowItem;

            default -> true;
        };
    }

    private RuneStatType getRuneStatType(ItemStack rune) {
        RuneStats stats = RuneItem.getRuneStats(rune);
        if (stats == null || stats.isEmpty()) return null;

        for (RuneStatType t : stats.view().keySet())
            return t;

        return null;
    }

    /* ============================================================
       PREVIEW LOGIC
       ============================================================ */

    private ItemStack preview(ItemStack target, ItemStack rune) {
        if (target.isEmpty() || rune.isEmpty()) return ItemStack.EMPTY;

        if (!canApplyRuneTo(target, rune)) return ItemStack.EMPTY;

        if (!rune.is(ModItems.ENHANCED_RUNE.get())) return ItemStack.EMPTY;
        if (RuneSlots.remaining(target) <= 0) return ItemStack.EMPTY;

        // If rune has stats → allowed
        RuneStats stats = RuneItem.getRuneStats(rune);
        if (!stats.isEmpty()) return target.copy();

        // If rune has effect → allowed
        Holder<Enchantment> effect = RuneItem.getPrimaryEffectEnchantment(rune);
        if (effect != null && RuneItem.isEffectEnchantment(effect)) {
            return target.copy();
        }

        return ItemStack.EMPTY;
    }

    /* ============================================================
       APPLY RUNE
       ============================================================ */

    private void applyRuneOnTake(ItemStack taken, ItemStack rune) {

        RuneStats runeStats = RuneItem.getRuneStats(rune);
        boolean hasStats = !runeStats.isEmpty();

        ItemEnchantments stored = rune.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = rune.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        boolean hasEffect = !stored.isEmpty() || !direct.isEmpty();

        if (hasStats) {
            RuneStats base = RuneStats.get(taken);
            RuneStats rolled = RuneStats.rollForApplication(runeStats, this.level.random);
            RuneStats.set(taken, RuneStats.combine(base, rolled));
            RuneSlots.tryConsumeSlot(taken);
        }

        if (hasEffect) {
            RuneSlots.tryConsumeSlot(taken);
        }
    }

    /* ============================================================
       MISC
       ============================================================ */

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
        if (!slot.hasItem()) return ItemStack.EMPTY;

        int resultSlot = 2;
        int invStart = 3;
        int invEnd = invStart + 36;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == resultSlot) {
            ItemStack rune = input.getItem(1).copy();
            ItemStack taken = stack.copy();

            result.setItem(0, ItemStack.EMPTY);
            slot.set(ItemStack.EMPTY);

            applyRuneOnTake(taken, rune);
            consumeInputs();
            playUseSound();
            updateResult();

            if (!this.moveItemStackTo(taken, invStart, invEnd, true))
                return ItemStack.EMPTY;

            return copy;
        }

        if (index < invStart) {
            if (!this.moveItemStackTo(stack, invStart, invEnd, false))
                return ItemStack.EMPTY;
        } else {
            if (stack.is(ModItems.ENHANCED_RUNE.get()) ||
                    stack.is(ModItems.REPAIR_RUNE.get()) ||
                    stack.is(ModItems.EXPANSION_RUNE.get()) ||
                    stack.is(ModItems.NULLIFICATION_RUNE.get()) ||
                    stack.is(ModItems.UPGRADE_RUNE.get())) {

                if (!this.moveItemStackTo(stack, 1, 2, false))
                    return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(stack, 0, 1, false))
                    return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }
}
