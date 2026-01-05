package net.revilodev.runic.screen.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.network.payload.PlaceEtchingRecipePayload;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.screen.custom.recipe.CraftableToggleButton;
import net.revilodev.runic.screen.custom.recipe.EtchingRecipeBookPanel;
import net.revilodev.runic.screen.custom.recipe.RecipeBookButton;
import net.revilodev.runic.screen.custom.recipe.RecipePageButton;

public final class EtchingTableScreen extends AbstractContainerScreen<EtchingTableMenu> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/container/etching_table.png");

    private static final int PANEL_W = 147;
    private static final int PANEL_H = 166;

    private boolean recipeBookVisible;

    private EtchingRecipeBookPanel recipeBook;

    private RecipeBookButton recipeButton;
    private EditBox searchBox;
    private CraftableToggleButton craftableToggle;
    private RecipePageButton pageBack;
    private RecipePageButton pageForward;

    private RecipeHolder<EtchingTableRecipe> ghostRecipe;

    public EtchingTableScreen(EtchingTableMenu menu, net.minecraft.world.entity.player.Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 40;
        this.titleLabelY = 20;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();

        this.recipeBook = this.addRenderableWidget(new EtchingRecipeBookPanel());
        this.recipeBook.setOnSelect(this::selectRecipe);
        this.recipeBook.visible = false;

        this.recipeButton = this.addRenderableWidget(new RecipeBookButton(0, 0, () -> {
            this.recipeBookVisible = !this.recipeBookVisible;
            this.ghostRecipe = null;
            updateLayout();
            if (this.recipeBookVisible) this.recipeBook.refresh();
        }));

        this.searchBox = new EditBox(this.font, 0, 0, 81, 9, Component.empty());
        this.searchBox.setBordered(false);
        this.searchBox.setMaxLength(50);
        this.searchBox.setTextColor(0xFF000000);
        this.searchBox.setResponder(s -> this.recipeBook.setSearch(s));
        this.addRenderableWidget(this.searchBox);

        this.craftableToggle = this.addRenderableWidget(new CraftableToggleButton(0, 0, craftableOnly -> {
            this.recipeBook.setCraftableOnly(craftableOnly);
            syncPageButtons();
        }));

        this.pageBack = this.addRenderableWidget(RecipePageButton.backward(0, 0, () -> {
            this.recipeBook.prevPage();
            syncPageButtons();
        }));

        this.pageForward = this.addRenderableWidget(RecipePageButton.forward(0, 0, () -> {
            this.recipeBook.nextPage();
            syncPageButtons();
        }));

        this.recipeBook.refresh();
        updateLayout();
        syncPageButtons();
    }

    private void updateLayout() {
        this.topPos = (this.height - this.imageHeight) / 2;

        if (this.recipeBookVisible) {
            int total = this.imageWidth + PANEL_W;
            int start = (this.width - total) / 2;
            this.leftPos = start + PANEL_W;
        } else {
            this.leftPos = (this.width - this.imageWidth) / 2;
        }

        int btnX = this.leftPos + 8 + (EtchingTableMenu.TOP_SLOT_X_OFFSET - 20) / 2;
        int btnY = this.topPos + 49;
        this.recipeButton.setX(btnX);
        this.recipeButton.setY(btnY);
        this.recipeButton.setSelected(this.recipeBookVisible);

        int panelX = this.leftPos - PANEL_W - 2;
        int panelY = this.topPos - 1;

        this.recipeBook.visible = this.recipeBookVisible;
        this.recipeBook.setBounds(panelX, panelY, PANEL_W, PANEL_H);

        this.searchBox.visible = this.recipeBookVisible;
        this.searchBox.active = this.recipeBookVisible;
        this.searchBox.setX(panelX + 25);
        this.searchBox.setY(panelY + 15);

        this.craftableToggle.visible = this.recipeBookVisible;
        this.craftableToggle.active = this.recipeBookVisible;
        this.craftableToggle.setX(panelX + PANEL_W - 26 - 8 - 3);
        this.craftableToggle.setY(panelY + 14 - 4);

        this.pageBack.visible = this.recipeBookVisible;
        this.pageBack.active = this.recipeBookVisible;

        this.pageForward.visible = this.recipeBookVisible;
        this.pageForward.active = this.recipeBookVisible;

        int pageY = panelY + 137;
        this.pageBack.setX(panelX + 94);
        this.pageBack.setY(pageY);

        this.pageForward.setX(panelX + 116);
        this.pageForward.setY(pageY);

        syncPageButtons();
    }

    private void syncPageButtons() {
        if (this.pageBack == null) return;
        boolean vis = this.recipeBookVisible && this.recipeBook.getTotalPages() > 1;
        this.pageBack.visible = vis;
        this.pageForward.visible = vis;
        this.pageBack.active = vis && this.recipeBook.canGoBack();
        this.pageForward.active = vis && this.recipeBook.canGoForward();
    }

    private void selectRecipe(RecipeHolder<EtchingTableRecipe> holder) {
        this.ghostRecipe = holder;
        PacketDistributor.sendToServer(new PlaceEtchingRecipePayload(holder.id()));
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        gg.blit(TEX, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 176, 166);
        syncPageButtons();
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.render(gg, mouseX, mouseY, partialTick);

        gg.pose().pushPose();
        gg.pose().translate(0, 0, 300);
        renderGhostRecipe(gg);
        gg.pose().popPose();

        if (this.recipeBookVisible) {
            gg.pose().pushPose();
            gg.pose().translate(0, 0, 400);

            this.searchBox.render(gg, mouseX, mouseY, partialTick);
            this.craftableToggle.render(gg, mouseX, mouseY, partialTick);
            if (this.pageBack.visible) this.pageBack.render(gg, mouseX, mouseY, partialTick);
            if (this.pageForward.visible) this.pageForward.render(gg, mouseX, mouseY, partialTick);

            gg.pose().popPose();
        }

        this.renderTooltip(gg, mouseX, mouseY);
    }


    @Override
    protected void renderTooltip(GuiGraphics gg, int mouseX, int mouseY) {
        if (this.recipeBookVisible) {
            ItemStack hovered = this.recipeBook.getHoveredStack();
            if (!hovered.isEmpty()) {
                gg.renderTooltip(this.font, hovered, mouseX, mouseY);
            }
        }
        super.renderTooltip(gg, mouseX, mouseY);
    }



    private void renderGhostRecipe(GuiGraphics gg) {
        if (this.ghostRecipe == null) return;

        EtchingTableRecipe r = this.ghostRecipe.value();
        renderGhostIngredient(gg, r.base(), this.menu.getSlot(0));
        renderGhostIngredient(gg, r.material(), this.menu.getSlot(1));
    }

    private void renderGhostIngredient(GuiGraphics gg, Ingredient ing, Slot slot) {
        ItemStack inSlot = slot.getItem();
        if (!inSlot.isEmpty() && ing.test(inSlot)) return;

        ItemStack ghost = firstStack(ing);
        if (ghost.isEmpty()) return;

        int x = this.leftPos + slot.x;
        int y = this.topPos + slot.y;

        gg.fill(x, y, x + 16, y + 16, 0x40FF0000);
        gg.renderItem(ghost, x, y);
        gg.renderItemDecorations(this.font, ghost, x, y);
    }

    private static ItemStack firstStack(Ingredient ing) {
        ItemStack[] items = ing.getItems();
        if (items.length == 0) return ItemStack.EMPTY;
        return items[0].copy();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        if (button == 0) {
            if (this.recipeBookVisible) {
                if (this.recipeBook.isMouseOver(mouseX, mouseY)) return handled;
                if (this.searchBox.isMouseOver(mouseX, mouseY)) return handled;
                if (this.craftableToggle.isMouseOver(mouseX, mouseY)) return handled;
                if (this.pageBack.visible && this.pageBack.isMouseOver(mouseX, mouseY)) return handled;
                if (this.pageForward.visible && this.pageForward.isMouseOver(mouseX, mouseY)) return handled;
            }
            this.ghostRecipe = null;
        }

        return handled;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.recipeBookVisible && this.recipeBook.isMouseOver(mouseX, mouseY)) {
            if (this.recipeBook.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                syncPageButtons();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int width, int height) {
        String keep = this.searchBox == null ? "" : this.searchBox.getValue();
        super.resize(minecraft, width, height);
        if (this.searchBox != null) this.searchBox.setValue(keep);
        updateLayout();
    }
}
