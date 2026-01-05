package net.revilodev.runic.screen.custom.recipe;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.runic.RunicMod;

public final class IconToggleButton extends AbstractWidget {
    private static final ResourceLocation ALL =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/all.png");
    private static final ResourceLocation ALL_H =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/all_highlighted.png");
    private static final ResourceLocation CRAFT =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/craftable.png");
    private static final ResourceLocation CRAFT_H =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/craftable_highlighted.png");

    private final ResourceLocation normal;
    private final ResourceLocation hover;
    private final Runnable onPress;

    private boolean selected;

    private IconToggleButton(int x, int y, ResourceLocation normal, ResourceLocation hover, Runnable onPress) {
        super(x, y, 20, 18, Component.empty());
        this.normal = normal;
        this.hover = hover;
        this.onPress = onPress;
    }

    public static IconToggleButton all(int x, int y, Runnable onPress) {
        return new IconToggleButton(x, y, ALL, ALL_H, onPress);
    }

    public static IconToggleButton craftable(int x, int y, Runnable onPress) {
        return new IconToggleButton(x, y, CRAFT, CRAFT_H, onPress);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        ResourceLocation tex = (this.isHoveredOrFocused() || this.selected) ? this.hover : this.normal;
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
