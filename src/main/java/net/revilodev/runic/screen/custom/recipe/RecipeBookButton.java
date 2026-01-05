package net.revilodev.runic.screen.custom.recipe;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.runic.RunicMod;

public final class RecipeBookButton extends AbstractWidget {
    private static final ResourceLocation NORMAL =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/recipe_button.png");
    private static final ResourceLocation HOVER =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/recipe_button_highlighted.png");

    private final Runnable onPress;
    private boolean selected;

    public RecipeBookButton(int x, int y, Runnable onPress) {
        super(x, y, 20, 18, Component.empty());
        this.onPress = onPress;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        ResourceLocation tex = (this.isHoveredOrFocused() || this.selected) ? HOVER : NORMAL;
        gg.blit(tex, getX(), getY(), 0, 0, 20, 18, 20, 18);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
