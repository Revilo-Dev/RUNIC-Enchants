package net.revilodev.runic.screen.custom.recipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.recipe.EtchingTableInput;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.recipe.ModRecipeTypes;
import net.revilodev.runic.screen.custom.EtchingTableMenu;
import net.revilodev.runic.stat.RuneStatType;

import java.util.ArrayList;
import java.util.Comparator;
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
    private Component hoveredRecipeName = Component.empty();

    private String search = "";
    private boolean craftableOnly;

    private int page;
    private int totalPages = 1;

    public EtchingRecipeBookPanel() {
        super(0, 0, 147, 166, Component.empty());
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

    public ItemStack getHoveredStack() {
        return hoveredStack;
    }

    public Component getHoveredRecipeName() {
        return hoveredRecipeName;
    }

    public void refresh() {
        Minecraft mc = Minecraft.getInstance();
        this.all.clear();
        this.filtered.clear();
        this.hoveredStack = ItemStack.EMPTY;
        this.hoveredRecipeName = Component.empty();
        this.page = 0;
        this.totalPages = 1;

        if (mc.level == null) return;

        this.all.addAll(mc.level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.ETCHING_TABLE.get())
                .stream()
                .filter(this::isVisibleForLoadedMods)
                .toList());
        this.all.sort(Comparator.comparing(h -> recipeDisplayName(h).getString().toLowerCase(Locale.ROOT)));

        rebuildFiltered();
    }

    public void setSearch(String s) {
        this.search = s == null ? "" : s;
        this.page = 0;
        rebuildFiltered();
    }

    public void setCraftableOnly(boolean craftableOnly) {
        this.craftableOnly = craftableOnly;
        this.page = 0;
        rebuildFiltered();
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageIndex() {
        return page;
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
        this.hoveredRecipeName = Component.empty();
        if (mc.level == null) return;

        String q = this.search.toLowerCase(Locale.ROOT).trim();

        for (RecipeHolder<EtchingTableRecipe> h : this.all) {
            if (!q.isEmpty()) {
                String name = recipeDisplayName(h).getString().toLowerCase(Locale.ROOT);
                if (!name.contains(q)) continue;
            }

            if (this.craftableOnly && !isCraftable(mc, h)) continue;

            this.filtered.add(h);
        }

        this.totalPages = Math.max(1, (this.filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (this.page >= this.totalPages) this.page = this.totalPages - 1;
    }

    private Component recipeDisplayName(RecipeHolder<EtchingTableRecipe> h) {
        EtchingTableRecipe r = h.value();

        if (r.stat().isPresent()) {
            RuneStatType stat = r.stat().get();
            return Component.literal(titleize(stat.id()));
        }

        if (r.effect().isPresent()) {
            ResourceLocation enchId = r.effect().get();
            String key = "enchantment." + enchId.getNamespace() + "." + enchId.getPath();
            Component tr = Component.translatable(key);
            String resolved = tr.getString();
            if (!resolved.equals(key)) return tr;
            return Component.literal(titleize(enchId.getPath()));
        }

        return r.result().getHoverName();
    }

    private boolean isVisibleForLoadedMods(RecipeHolder<EtchingTableRecipe> h) {
        String sourceMod = compatSourceModFromRecipeId(h.id());
        if (sourceMod != null && !sourceMod.equals("minecraft") && !sourceMod.equals(RunicMod.MOD_ID)) {
            if (!ModList.get().isLoaded(sourceMod)) return false;
        }

        var effect = h.value().effect();
        if (effect.isEmpty()) return true;

        String namespace = effect.get().getNamespace();
        if (namespace.equals("minecraft") || namespace.equals(RunicMod.MOD_ID)) return true;

        return ModList.get().isLoaded(namespace);
    }

    private static String compatSourceModFromRecipeId(ResourceLocation recipeId) {
        String[] parts = recipeId.getPath().split("/");
        if (parts.length >= 3 && parts[0].equals("etching_table") && parts[1].equals("effect")) {
            return parts[2];
        }
        return null;
    }

    private static String titleize(String id) {
        if (id == null || id.isBlank()) return "";
        String[] parts = id.split("[_\\-]+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1));
        }
        return sb.toString();
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        hoveredStack = ItemStack.EMPTY;
        hoveredRecipeName = Component.empty();

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

            ItemStack out = previewStack(mc, holder);
            gg.renderItem(out, bx + 4, by + 4);
            gg.renderItemDecorations(mc.font, out, bx + 4, by + 4);

            if (mouseX >= bx && mouseX < bx + 25 && mouseY >= by && mouseY < by + 25) {
                hoveredStack = out;
                hoveredRecipeName = recipeDisplayName(holder);
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

    private ItemStack previewStack(Minecraft mc, RecipeHolder<EtchingTableRecipe> holder) {
        if (mc.level == null) return holder.value().result().copy();

        ItemStack out = holder.value().assemble(
                new EtchingTableInput(ItemStack.EMPTY, ItemStack.EMPTY),
                mc.level.registryAccess()
        );

        return out.isEmpty() ? holder.value().result().copy() : out;
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
