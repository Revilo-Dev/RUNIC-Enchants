// src/main/java/net/revilodev/runic/screen/custom/ForgeButton.java
package net.revilodev.runic.screen.custom;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.revilodev.runic.RunicMod;

public final class ForgeButton extends AbstractButton {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/forge_button.png");
    private static final ResourceLocation TEX_H =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/widget/forge_button_highlighted.png");

    private final Runnable onPress;

    public ForgeButton(int x, int y, Runnable onPress) {
        super(x, y, 19, 18, Component.empty());
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        if (this.active && onPress != null) onPress.run();
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.active && this.isMouseOver(mouseX, mouseY);
        gg.blit(hovered ? TEX_H : TEX, getX(), getY(), 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
    }
}
