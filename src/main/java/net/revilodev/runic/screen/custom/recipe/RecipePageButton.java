package net.revilodev.runic.screen.custom.recipe;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.runic.RunicMod;

public final class RecipePageButton extends AbstractWidget {
    private static final int W = 12;
    private static final int H = 17;

    private static final ResourceLocation BACK =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/page_backward.png");
    private static final ResourceLocation BACK_H =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/page_backward_highlighted.png");
    private static final ResourceLocation FWD =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/page_forward.png");
    private static final ResourceLocation FWD_H =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/page_forward_highlighted.png");

    private final boolean forward;
    private final Runnable onPress;

    private RecipePageButton(int x, int y, boolean forward, Runnable onPress) {
        super(x, y, W, H, Component.empty());
        this.forward = forward;
        this.onPress = onPress;
    }

    public static RecipePageButton backward(int x, int y, Runnable onPress) {
        return new RecipePageButton(x, y, false, onPress);
    }

    public static RecipePageButton forward(int x, int y, Runnable onPress) {
        return new RecipePageButton(x, y, true, onPress);
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        ResourceLocation tex;
        if (forward) tex = this.isHoveredOrFocused() ? FWD_H : FWD;
        else tex = this.isHoveredOrFocused() ? BACK_H : BACK;

        gg.blit(tex, getX(), getY(), 0, 0, W, H, W, H);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.active) this.onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
