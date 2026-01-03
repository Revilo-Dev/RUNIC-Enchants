package net.revilodev.runic.screen.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.revilodev.runic.RunicMod;

public final class EtchingTableScreen extends AbstractContainerScreen<EtchingTableMenu> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/container/etching_table.png");

    private EtchingRecipeBookPanel recipeBook;
    private Button recipeButton;

    public EtchingTableScreen(EtchingTableMenu menu, Inventory inv, Component title) {
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

        int panelW = 140;
        int panelX = this.leftPos - panelW - 6;
        int panelY = this.topPos;

        this.recipeBook = new EtchingRecipeBookPanel(this, panelX, panelY, panelW, this.imageHeight);
        this.recipeBook.refreshRecipes();
        this.addRenderableWidget(this.recipeBook);

        this.recipeButton = this.addRenderableWidget(
                Button.builder(Component.literal("Recipes"), b -> toggleRecipeBook())
                        .pos(this.leftPos + this.imageWidth - 62, this.topPos + 6)
                        .size(56, 18)
                        .build()
        );
    }

    private void toggleRecipeBook() {
        boolean next = !this.recipeBook.visible;
        this.recipeBook.visible = next;
        if (next) this.recipeBook.refreshRecipes();
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        gg.blit(TEX, x, y, 0, 0, this.imageWidth, this.imageHeight, 176, 166);
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg, mouseX, mouseY, partialTick);
        super.render(gg, mouseX, mouseY, partialTick);

        if (this.recipeBook != null && this.recipeBook.visible) {
            var hovered = this.recipeBook.hoveredStack();
            if (!hovered.isEmpty()) gg.renderTooltip(this.font, hovered, mouseX, mouseY);
        }

        this.renderTooltip(gg, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        gg.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        gg.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
