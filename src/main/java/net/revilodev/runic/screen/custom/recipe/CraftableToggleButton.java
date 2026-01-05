package net.revilodev.runic.screen.custom.recipe;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.runic.RunicMod;

import java.util.function.Consumer;

public final class CraftableToggleButton extends AbstractWidget {
    private static final ResourceLocation ALL =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/all.png");
    private static final ResourceLocation ALL_H =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/all_highlighted.png");
    private static final ResourceLocation CRAFT =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/craftable.png");
    private static final ResourceLocation CRAFT_H =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/craftable_highlighted.png");

    private final Consumer<Boolean> onToggle;
    private boolean craftableOnly;

    public CraftableToggleButton(int x, int y, Consumer<Boolean> onToggle) {
        super(x, y, 26, 16, Component.empty());
        this.onToggle = onToggle;
    }

    public boolean isCraftableOnly() {
        return craftableOnly;
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        boolean hover = this.isHoveredOrFocused();
        ResourceLocation tex;
        if (craftableOnly) tex = hover ? CRAFT_H : CRAFT;
        else tex = hover ? ALL_H : ALL;
        gg.blit(tex, getX(), getY(), 0, 0, 26, 16, 26, 16);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.craftableOnly = !this.craftableOnly;
        this.onToggle.accept(this.craftableOnly);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
