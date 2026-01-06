package net.revilodev.runic.screen.custom.recipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.recipe.ModRecipeTypes;
import net.revilodev.runic.screen.custom.EtchingTableMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public final class EtchingRecipeBookPanel extends AbstractWidget {
    private static final ResourceLocation PANEL =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/recipe_book.png");
    private static final ResourceLocation SLOT_C =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/slot_craftable.png");
    private static final ResourceLocation SLOT_U =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/slot_uncraftable.png");

    private static final int COLS = 5;
    private static final int ROWS = 4;
    private static final int PAGE_SIZE = COLS * ROWS;

    private static final int SLOT = 25;
    private static final int GRID_X = 11;
    private static final int GRID_Y = 33;

    private final List<RecipeHolder<EtchingTableRecipe>> all = new ArrayList<>();
    private final List<RecipeHolder<EtchingTableRecipe>> filtered = new ArrayList<>();

    private Consumer<RecipeHolder<EtchingTableRecipe>> onSelect = h -> {};
    private ItemStack hoveredStack = ItemStack.EMPTY;

    private String search = "";
    private boolean craftableOnly;

    private int page;
    private int totalPages = 1;

    public EtchingRecipeBookPanel() {
        super(0, 0, 147, 166, net.minecraft.network.chat.Component.empty());
    }

    public void setBounds(int x, int y, int w, int h) {
        this.setX(x);
        this.setY(y);
        this.width = w;
        this.height = h;
    }

    public void setOnSelect(Consumer<RecipeHolder<EtchingTableRecipe>> onSelect) {
        this.onSelect = onSelect == null ? h -> {} : onSelect;
    }

    public void setSearch(String search) {
        String s = (search == null) ? "" : search;
        if (this.search.equals(s)) return;
        this.search = s;
        this.page = 0;
        rebuildFiltered();
    }

    public ItemStack getHoveredStack() {
        return hoveredStack;
    }

    public void refresh() {
        Minecraft mc = Minecraft.getInstance();
        this.all.clear();
        this.filtered.clear();
        this.hoveredStack = ItemStack.EMPTY;
        this.page = 0;
        this.totalPages = 1;

        if (mc.level == null) return;

        this.all.addAll(mc.level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.ETCHING_TABLE.get()));
        rebuildFiltered();
    }

    private String searchNameFor(RecipeHolder<EtchingTableRecipe> h) {
        ItemStack out = h.value().result().copy();

        String ench = firstEnchantmentName(out);
        if (!ench.isEmpty()) return ench;

        if (h.value().effect().isPresent()) {
            String eff = effectName(h.value().effect().get());
            if (!eff.isEmpty()) return eff;
        }

        return out.getHoverName().getString();
    }

    private String firstEnchantmentName(ItemStack stack) {
        var ench = stack.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        if (!ench.isEmpty()) {
            var it = ench.entrySet().iterator();
            if (it.hasNext()) return it.next().getKey().value().description().getString();
        }

        var stored = stack.getOrDefault(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        if (!stored.isEmpty()) {
            var it = stored.entrySet().iterator();
            if (it.hasNext()) return it.next().getKey().value().description().getString();
        }

        return "";
    }

    private String effectName(net.minecraft.resources.ResourceLocation id) {
        var eff = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getOptional(id);
        return eff.map(e -> e.getDisplayName().getString()).orElse("");
    }

    public void setCraftableOnly(boolean craftableOnly) {
        this.craftableOnly = craftableOnly;
        this.page = 0;
        rebuildFiltered();
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean canGoBack() {
        return page > 0;
    }

    public boolean canGoForward() {
        return page + 1 < totalPages;
    }

    public void prevPage() {
        if (page > 0) page--;
    }

    public void nextPage() {
        if (page + 1 < totalPages) page++;
    }

    private void rebuildFiltered() {
        Minecraft mc = Minecraft.getInstance();
        this.filtered.clear();
        this.hoveredStack = ItemStack.EMPTY;
        if (mc.level == null) return;

        String q = this.search.toLowerCase(Locale.ROOT).trim();

        for (RecipeHolder<EtchingTableRecipe> h : this.all) {
            String name = searchNameFor(h).toLowerCase(Locale.ROOT);

            if (!q.isEmpty() && !name.contains(q)) continue;
            if (this.craftableOnly && !isCraftable(mc, h)) continue;

            this.filtered.add(h);
        }

        this.totalPages = Math.max(1, (this.filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (this.page >= this.totalPages) this.page = this.totalPages - 1;
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        hoveredStack = ItemStack.EMPTY;

        gg.blit(PANEL, getX(), getY(), 0, 0, width, height, 256, 256);

        Minecraft mc = Minecraft.getInstance();

        int start = page * PAGE_SIZE;
        int end = Math.min(filtered.size(), start + PAGE_SIZE);

        for (int i = start; i < end; i++) {
            int rel = i - start;
            int col = rel % COLS;
            int row = rel / COLS;

            int bx = getX() + GRID_X + col * SLOT;
            int by = getY() + GRID_Y + row * SLOT;

            RecipeHolder<EtchingTableRecipe> holder = filtered.get(i);
            boolean craftable = isCraftable(mc, holder);

            gg.blit(craftable ? SLOT_C : SLOT_U, bx, by, 0, 0, 25, 25, 25, 25);

            ItemStack out = holder.value().result().copy();
            gg.renderItem(out, bx + 4, by + 4);
            gg.renderItemDecorations(mc.font, out, bx + 4, by + 4);

            if (mouseX >= bx && mouseX < bx + 25 && mouseY >= by && mouseY < by + 25) {
                hoveredStack = out;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible) return false;
        if (button != 0) return false;
        if (!this.isMouseOver(mouseX, mouseY)) return false;

        int start = page * PAGE_SIZE;
        int end = Math.min(filtered.size(), start + PAGE_SIZE);

        for (int i = start; i < end; i++) {
            int rel = i - start;
            int col = rel % COLS;
            int row = rel / COLS;

            int bx = getX() + GRID_X + col * SLOT;
            int by = getY() + GRID_Y + row * SLOT;

            if (mouseX >= bx && mouseX < bx + 25 && mouseY >= by && mouseY < by + 25) {
                onSelect.accept(filtered.get(i));
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.visible) return false;
        if (!this.isMouseOver(mouseX, mouseY)) return false;
        if (this.totalPages <= 1) return true;

        if (verticalAmount < 0) nextPage();
        else if (verticalAmount > 0) prevPage();

        return true;
    }

    public int getPageIndex() {
        return page;
    }

    private boolean isCraftable(Minecraft mc, RecipeHolder<EtchingTableRecipe> holder) {
        if (mc.player == null) return false;
        if (!(mc.player.containerMenu instanceof EtchingTableMenu menu)) return false;

        Ingredient a = holder.value().base();
        Ingredient b = holder.value().material();

        Inventory inv = mc.player.getInventory();

        List<ItemStack> stacks = new ArrayList<>(inv.getContainerSize() + 2);
        for (int i = 0; i < inv.getContainerSize(); i++) stacks.add(inv.getItem(i));
        stacks.add(menu.getSlot(0).getItem());
        stacks.add(menu.getSlot(1).getItem());

        int[] counts = new int[stacks.size()];
        for (int i = 0; i < stacks.size(); i++) counts[i] = stacks.get(i).isEmpty() ? 0 : stacks.get(i).getCount();

        return takeOne(stacks, counts, a) && takeOne(stacks, counts, b);
    }

    private boolean takeOne(List<ItemStack> stacks, int[] counts, Ingredient ing) {
        for (int i = 0; i < stacks.size(); i++) {
            if (counts[i] <= 0) continue;
            ItemStack st = stacks.get(i);
            if (st.isEmpty()) continue;
            if (!ing.test(st)) continue;
            counts[i]--;
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
