// src/main/java/net/revilodev/runic/screen/custom/ArtisansWorkbenchScreen.java
package net.revilodev.runic.screen.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.client.ArtisansPreviewRenderer;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.*;

public final class ArtisansWorkbenchScreen extends AbstractContainerScreen<ArtisansWorkbenchMenu> {
    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/container/artisans_workbench.png");

    private static final int PREVIEW_X = 116;
    private static final int PREVIEW_Y = 16;
    private static final int PREVIEW_W = 52;
    private static final int PREVIEW_H = 52;

    private static final int FORGE_X = 52;
    private static final int FORGE_Y = 51;

    private ForgeButton forgeButton;

    public ArtisansWorkbenchScreen(ArtisansWorkbenchMenu menu, Inventory inv, Component ignoredTitle) {
        super(menu, inv, Component.literal("Apply Upgrades"));
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 40;
        this.titleLabelY = 17;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();
        this.forgeButton = this.addRenderableWidget(new ForgeButton(this.leftPos + FORGE_X, this.topPos + FORGE_Y, this::pressForge));
        syncButtonState();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        syncButtonState();
    }

    private void syncButtonState() {
        if (this.forgeButton == null) return;
        this.forgeButton.setPosition(this.leftPos + FORGE_X, this.topPos + FORGE_Y);
        this.forgeButton.active = !this.menu.getPreviewStack().isEmpty();
        this.forgeButton.visible = true;
    }

    private void pressForge() {
        Minecraft mc = this.minecraft;
        if (mc == null || mc.gameMode == null) return;
        mc.gameMode.handleInventoryButtonClick(this.menu.containerId, ArtisansWorkbenchMenu.BUTTON_FORGE);
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        gg.blit(TEX, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 176, 166);

        ItemStack preview = this.menu.getPreviewStack();
        ItemStack base = this.menu.getGearStack();
        ItemStack toPreview = !preview.isEmpty() ? preview : base;

        if (!toPreview.isEmpty()) {
            ArtisansPreviewRenderer.render(
                    gg,
                    toPreview,
                    this.leftPos + PREVIEW_X,
                    this.topPos + PREVIEW_Y,
                    PREVIEW_W,
                    PREVIEW_H,
                    partialTick
            );
        }
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.render(gg, mouseX, mouseY, partialTick);
        renderForgePreviewTooltip(gg);
        this.renderTooltip(gg, mouseX, mouseY);
    }

    /**
     * Renders a stable "Changes" tooltip panel to the right of the GUI (or left if no space).
     * This does NOT use the item's full tooltip, so it won't show "When in main hand" blocks.
     */
    private void renderForgePreviewTooltip(GuiGraphics gg) {
        ItemStack base = this.menu.getGearStack();
        ItemStack preview = this.menu.getPreviewStack();
        if (base.isEmpty() || preview.isEmpty() || this.minecraft == null) return;

        List<Component> delta = buildDeltaLines(base, preview);
        if (delta.isEmpty()) return;

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("Changes").withStyle(ChatFormatting.GRAY));
        lines.addAll(delta);

        // Place panel to the right of the GUI
        int x = this.leftPos + this.imageWidth + 8;
        int y = this.topPos + 6;

        int tw = 0;
        for (Component c : lines) tw = Math.max(tw, this.font.width(c));
        int th = lines.size() * this.font.lineHeight;

        // If it doesn't fit on the right, move to the left of the GUI
        if (x + tw + 12 > this.width) {
            x = this.leftPos - tw - 20;
        }

        // Clamp on screen (never "return" and disappear)
        x = Math.max(6, Math.min(x, this.width - tw - 12));
        y = Math.max(6, Math.min(y, this.height - th - 12));

        gg.renderTooltip(this.font, lines, Optional.empty(), x, y);
    }

    private List<Component> buildDeltaLines(ItemStack base, ItemStack preview) {
        List<Component> out = new ArrayList<>();

        // Rune slots (show absolute base -> preview)
        int baseCap = RuneSlots.capacity(base);
        int prevCap = RuneSlots.capacity(preview);
        int baseUsed = RuneSlots.used(base);
        int prevUsed = RuneSlots.used(preview);

        if (baseCap != prevCap || baseUsed != prevUsed) {
            String a = baseUsed + "/" + baseCap;
            String b = prevUsed + "/" + prevCap;
            out.add(Component.literal("Rune slots: " + a + " \u2192 " + b).withStyle(ChatFormatting.AQUA));
        }

        // Durability (show max + remaining deltas)
        if (base.isDamageableItem() && preview.isDamageableItem()) {
            int baseMax = base.getMaxDamage();
            int prevMax = preview.getMaxDamage();
            int dMax = prevMax - baseMax;
            if (dMax != 0) out.add(coloredDelta("Max Durability", dMax));

            int baseRem = baseMax - base.getDamageValue();
            int prevRem = prevMax - preview.getDamageValue();
            int dRem = prevRem - baseRem;
            if (dRem != 0) out.add(coloredDelta("Durability (remaining)", dRem));
        }

        // Rune stats (percent deltas)
        RuneStats bStats = RuneStats.get(base);
        RuneStats pStats = RuneStats.get(preview);

        Map<RuneStatType, Float> bm = bStats == null ? Map.of() : bStats.view();
        Map<RuneStatType, Float> pm = pStats == null ? Map.of() : pStats.view();

        if (!bm.isEmpty() || !pm.isEmpty()) {
            List<RuneStatType> keys = new ArrayList<>();
            keys.addAll(bm.keySet());
            for (RuneStatType t : pm.keySet()) if (!keys.contains(t)) keys.add(t);
            keys.sort(Comparator.comparing(RuneStatType::id));

            for (RuneStatType t : keys) {
                float bv = bm.getOrDefault(t, 0.0F);
                float pv = pm.getOrDefault(t, 0.0F);
                float dv = pv - bv;
                if (Math.abs(dv) <= 0.0001F) continue;
                out.add(coloredPercent(pretty(t.id()), dv));
            }
        }

        // Enchant deltas (level deltas)
        ItemEnchantments be = base.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments pe = preview.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (!be.isEmpty() || !pe.isEmpty()) {
            Map<String, Integer> bMap = enchMap(be);
            Map<String, Integer> pMap = enchMap(pe);

            List<String> eKeys = new ArrayList<>();
            eKeys.addAll(bMap.keySet());
            for (String k : pMap.keySet()) if (!eKeys.contains(k)) eKeys.add(k);
            eKeys.sort(String::compareTo);

            for (String k : eKeys) {
                int bl = bMap.getOrDefault(k, 0);
                int pl = pMap.getOrDefault(k, 0);
                int dl = pl - bl;
                if (dl != 0) out.add(coloredDelta(k, dl));
            }
        }

        return out;
    }

    private static Map<String, Integer> enchMap(ItemEnchantments e) {
        Map<String, Integer> out = new HashMap<>();
        for (var it : e.entrySet()) {
            String name = it.getKey().value().description().getString();
            out.put(name, it.getIntValue());
        }
        return out;
    }

    private static Component coloredDelta(String label, int delta) {
        ChatFormatting fmt = delta > 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        String s = (delta > 0 ? "+" : "") + delta;
        return Component.literal(label + ": " + s).withStyle(fmt);
    }

    private static Component coloredPercent(String label, float dvRaw) {
        double pct = dvRaw;
        // If someone ever feeds fraction (0.18), treat as 18%
        if (Math.abs(pct) <= 2.0) pct = pct * 100.0;
        ChatFormatting fmt = pct > 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        String s = (pct > 0 ? "+" : "") + trimDouble(pct) + "%";
        return Component.literal(label + ": " + s).withStyle(fmt);
    }

    private static String trimDouble(double v) {
        double av = Math.abs(v);
        if (av >= 1000.0) return String.format(Locale.ROOT, "%.0f", v);
        if (av >= 100.0) return String.format(Locale.ROOT, "%.1f", v);
        if (av >= 10.0) return String.format(Locale.ROOT, "%.2f", v);
        if (av >= 1.0) return String.format(Locale.ROOT, "%.2f", v);
        if (av >= 0.1) return String.format(Locale.ROOT, "%.2f", v);
        return String.format(Locale.ROOT, "%.3f", v);
    }

    private static String pretty(String id) {
        if (id == null || id.isBlank()) return "";
        String[] parts = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        gg.pose().pushPose();
        float s = 0.85f;
        gg.pose().scale(s, s, 1f);
        int tx = (int) (this.titleLabelX / s);
        int ty = (int) (this.titleLabelY / s);
        gg.drawString(this.font, this.title, tx, ty, 0x404040, false);
        gg.pose().popPose();

        gg.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
