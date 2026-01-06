package net.revilodev.runic.screen.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.revilodev.runic.block.ModBlocks;
import net.revilodev.runic.recipe.EtchingTableInput;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.recipe.ModRecipeTypes;
import net.revilodev.runic.screen.ModMenuTypes;

import java.util.Optional;

public final class EtchingTableMenu extends AbstractContainerMenu {
    public static final int TOP_SLOT_X_OFFSET = 36;

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
    private RecipeHolder<EtchingTableRecipe> lastRecipe;

    private EtchingTableMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(ModMenuTypes.ETCHING_TABLE.get(), id);
        this.access = access;
        this.level = inv.player.level();

        this.addSlot(new Slot(input, 0, 8 + TOP_SLOT_X_OFFSET, 50) {
            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        this.addSlot(new Slot(input, 1, 44 + TOP_SLOT_X_OFFSET, 50));

        this.addSlot(new Slot(result, 0, 98 + TOP_SLOT_X_OFFSET, 50) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return !this.getItem().isEmpty() && EtchingTableMenu.this.lastRecipe != null;
            }

            @Override
            public void onTake(Player player, ItemStack taken) {
                EtchingTableMenu.this.craft(player);
                super.onTake(player, taken);
            }
        });

        int x = 8;
        int y = 84;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                this.addSlot(new Slot(inv, c + r * 9 + 9, x + c * 18, y + r * 18));
            }
        }

        for (int c = 0; c < 9; c++) {
            this.addSlot(new Slot(inv, c, x + c * 18, y + 58));
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
        ItemStack base = input.getItem(0);
        ItemStack mat = input.getItem(1);

        if (base.isEmpty() || mat.isEmpty()) {
            lastRecipe = null;
            result.setItem(0, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }

        EtchingTableInput in = new EtchingTableInput(base, mat);
        Optional<RecipeHolder<EtchingTableRecipe>> match =
                level.getRecipeManager().getRecipeFor(ModRecipeTypes.ETCHING_TABLE.get(), in, level);

        if (match.isPresent()) {
            lastRecipe = match.get();
            ItemStack out = lastRecipe.value().assemble(in, level.registryAccess());
            result.setItem(0, out);
        } else {
            lastRecipe = null;
            result.setItem(0, ItemStack.EMPTY);
        }

        broadcastChanges();
    }

    private void craft(Player player) {
        if (lastRecipe == null) return;

        ItemStack base = input.getItem(0);
        ItemStack mat = input.getItem(1);

        if (base.isEmpty() || mat.isEmpty()) return;

        EtchingTableInput in = new EtchingTableInput(base, mat);
        if (!lastRecipe.value().matches(in, level)) return;

        input.getItem(0).shrink(1);
        input.getItem(1).shrink(1);

        result.setItem(0, ItemStack.EMPTY);
        updateResult();
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
            if (!this.moveItemStackTo(stack, invStart, invEnd, true)) return ItemStack.EMPTY;
            slot.onTake(player, copy);
            return copy;
        }

        if (index < invStart) {
            if (!this.moveItemStackTo(stack, invStart, invEnd, false)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(stack, 0, 2, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }

    public void placeRecipeFromBook(ServerPlayer player, RecipeHolder<EtchingTableRecipe> recipe) {
        if (player.containerMenu != this) return;

        Ingredient a = recipe.value().base();
        Ingredient b = recipe.value().material();

        Slot s0 = this.getSlot(0);
        Slot s1 = this.getSlot(1);

        ensureSlotMatchesOrClear(player, s0, a);
        ensureSlotMatchesOrClear(player, s1, b);

        if (!s0.hasItem()) pullOneIntoSlot(player, s0, a);
        if (!s1.hasItem()) pullOneIntoSlot(player, s1, b);

        updateResult();
        broadcastChanges();
    }

    private void ensureSlotMatchesOrClear(ServerPlayer player, Slot slot, Ingredient ing) {
        if (!slot.hasItem()) return;
        ItemStack cur = slot.getItem();
        if (ing.test(cur)) return;

        ItemStack toReturn = cur.copy();
        slot.set(ItemStack.EMPTY);
        if (!player.getInventory().add(toReturn)) {
            player.drop(toReturn, false);
        }
    }

    private void pullOneIntoSlot(ServerPlayer player, Slot dest, Ingredient ing) {
        for (int i = 3; i < 3 + 36; i++) {
            Slot src = this.getSlot(i);
            if (!src.hasItem()) continue;

            ItemStack st = src.getItem();
            if (!ing.test(st)) continue;

            ItemStack one = st.copy();
            one.setCount(1);

            st.shrink(1);
            src.setChanged();

            dest.set(one);
            dest.setChanged();
            return;
        }
    }
}
