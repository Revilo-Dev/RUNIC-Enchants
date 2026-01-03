package net.revilodev.runic.screen.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.recipe.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class EtchingRecipeBookPanel extends AbstractWidget {
    private static final int HEADER_H = 14;
    private static final int FOOTER_H = 46;
    private static final int ENTRY_H = 20;

    private final EtchingTableScreen screen;
    private List<RecipeHolder<EtchingTableRecipe>> recipes = List.of();

    private int scrollPx;
    private int selectedIndex = -1;
    private ItemStack hoveredStack = ItemStack.EMPTY;

    public EtchingRecipeBookPanel(EtchingTableScreen screen, int x, int y, int w, int h) {
        super(x, y, w, h, Component.empty());
        this.screen = screen;
        this.visible = false;
    }

    public void refreshRecipes() {
        var mc = Minecraft.getInstance();
        if (mc.level == null) {
            this.recipes = List.of();
            this.scrollPx = 0;
            this.selectedIndex = -1;
            return;
        }
        this.recipes = new ArrayList<>(mc.level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.ETCHING_TABLE.get()));
        this.scrollPx = 0;
        if (this.selectedIndex >= this.recipes.size()) this.selectedIndex = -1;
    }

    public void setPanelPosition(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    public ItemStack hoveredStack() {
        return hoveredStack;
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        hoveredStack = ItemStack.EMPTY;

        int x0 = getX();
        int y0 = getY();
        int x1 = x0 + width;
        int y1 = y0 + height;

        gg.fill(x0, y0, x1, y1, 0xCC000000);
        gg.renderOutline(x0, y0, width, height, 0xFF404040);

        Font font = Minecraft.getInstance().font;
        gg.drawString(font, Component.literal("Recipes"), x0 + 6, y0 + 4, 0xFFFFFF, false);

        int listTop = y0 + HEADER_H;
        int listBottom = y1 - FOOTER_H;
        int listH = Math.max(0, listBottom - listTop);

        int totalH = recipes.size() * ENTRY_H;
        int maxScroll = Math.max(0, totalH - listH);
        scrollPx = Mth.clamp(scrollPx, 0, maxScroll);

        int firstIndex = listH <= 0 ? 0 : (scrollPx / ENTRY_H);
        int yOffset = -(scrollPx % ENTRY_H);

        for (int i = firstIndex; i < recipes.size(); i++) {
            int rowY = listTop + yOffset + (i - firstIndex) * ENTRY_H;
            if (rowY >= listBottom) break;

            int rowX = x0 + 4;
            int rowW = width - 8;

            if (i == selectedIndex) gg.fill(rowX, rowY, rowX + rowW, rowY + ENTRY_H, 0x5533AAFF);

            ItemStack out = recipes.get(i).value().result().copy();
            int iconX = rowX + 2;
            int iconY = rowY + 2;

            gg.renderItem(out, iconX, iconY);
            gg.renderItemDecorations(font, out, iconX, iconY);

            int nameX = iconX + 20;
            int nameY = rowY + 6;
            gg.drawString(font, out.getHoverName(), nameX, nameY, 0xE0E0E0, false);

            if (mouseX >= iconX && mouseX < iconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
                hoveredStack = out;
            }

            if (mouseX >= rowX && mouseX < rowX + rowW && mouseY >= rowY && mouseY < rowY + ENTRY_H) {
                gg.renderOutline(rowX, rowY, rowW, ENTRY_H, 0xFF606060);
            }
        }

        int footerTop = y1 - FOOTER_H + 6;
        int footerX = x0 + 6;

        gg.drawString(font, Component.literal("Selected:"), footerX, footerTop, 0xFFFFFF, false);

        if (selectedIndex >= 0 && selectedIndex < recipes.size()) {
            EtchingTableRecipe r = recipes.get(selectedIndex).value();

            int iconsY = footerTop + 12;
            ItemStack base = firstStack(r.base());
            ItemStack mat = firstStack(r.material());
            ItemStack out = r.result().copy();

            gg.renderItem(base, footerX, iconsY);
            gg.renderItemDecorations(font, base, footerX, iconsY);

            gg.drawString(font, Component.literal("+"), footerX + 20, iconsY + 4, 0xFFFFFF, false);

            gg.renderItem(mat, footerX + 32, iconsY);
            gg.renderItemDecorations(font, mat, footerX + 32, iconsY);

            gg.drawString(font, Component.literal("→"), footerX + 52, iconsY + 4, 0xFFFFFF, false);

            gg.renderItem(out, footerX + 68, iconsY);
            gg.renderItemDecorations(font, out, footerX + 68, iconsY);

            Optional<?> stat = r.stat();
            Optional<?> effect = r.effect();

            int infoY = iconsY + 20;
            if (stat.isPresent()) gg.drawString(font, Component.literal("Stat: " + stat.get()), footerX, infoY, 0xC0C0C0, false);
            if (effect.isPresent()) gg.drawString(font, Component.literal("Effect: " + effect.get()), footerX, infoY + 10, 0xC0C0C0, false);
        }
    }

    private static ItemStack firstStack(Ingredient ing) {
        ItemStack[] items = ing.getItems();
        if (items.length == 0) return ItemStack.EMPTY;
        return items[0].copy();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!visible) return false;
        if (!isMouseOver(mouseX, mouseY)) return false;

        int listTop = getY() + HEADER_H;
        int listBottom = getY() + height - FOOTER_H;
        int listH = Math.max(0, listBottom - listTop);

        int totalH = recipes.size() * ENTRY_H;
        int maxScroll = Math.max(0, totalH - listH);

        scrollPx = Mth.clamp(scrollPx - (int) (verticalAmount * 12), 0, maxScroll);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        if (!isMouseOver(mouseX, mouseY)) return false;

        int listTop = getY() + HEADER_H;
        int listBottom = getY() + height - FOOTER_H;
        if (mouseY < listTop || mouseY >= listBottom) return false;

        int relY = (int) mouseY - listTop + scrollPx;
        int idx = relY / ENTRY_H;

        if (idx >= 0 && idx < recipes.size()) {
            selectedIndex = idx;
            return true;
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
