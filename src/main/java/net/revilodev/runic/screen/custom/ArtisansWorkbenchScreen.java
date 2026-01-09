// src/main/java/net/revilodev/runic/screen/custom/ArtisansWorkbenchScreen.java
package net.revilodev.runic.screen.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
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

    private static final String ATTR_ATTACK_DAMAGE = "attribute.name.generic.attack_damage";
    private static final String ATTR_ATTACK_SPEED = "attribute.name.generic.attack_speed";

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

    private void renderForgePreviewTooltip(GuiGraphics gg) {
        ItemStack base = this.menu.getGearStack();
        ItemStack preview = this.menu.getPreviewStack();
        if (base.isEmpty() || preview.isEmpty() || this.minecraft == null) return;

        List<Component> lines = new ArrayList<>(this.getTooltipFromItem(this.minecraft, preview));

        List<Component> delta = buildDeltaLines(base, preview);
        if (!delta.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.literal("Changes").withStyle(ChatFormatting.GRAY));
            lines.addAll(delta);
        }

        int x = this.leftPos + PREVIEW_X + PREVIEW_W + 10;
        int y = this.topPos + PREVIEW_Y;

        int tw = 0;
        for (Component c : lines) tw = Math.max(tw, this.font.width(c));
        int th = lines.size() * this.font.lineHeight;

        if (x + tw + 12 > this.width) return;
        if (y + th + 12 > this.height) return;

        gg.renderTooltip(this.font, lines, Optional.empty(), x, y);
    }

    private List<Component> buildDeltaLines(ItemStack base, ItemStack preview) {
        List<Component> out = new ArrayList<>();

        int baseCap = RuneSlots.capacity(base);
        int prevCap = RuneSlots.capacity(preview);
        int baseUsed = RuneSlots.used(base);
        int prevUsed = RuneSlots.used(preview);

        if (baseCap != prevCap || baseUsed != prevUsed) {
            String a = baseUsed + "/" + baseCap;
            String b = prevUsed + "/" + prevCap;
            out.add(Component.literal("Rune slots: " + a + " \u2192 " + b).withStyle(ChatFormatting.AQUA));
        }

        if (base.isDamageableItem() && preview.isDamageableItem()) {
            int baseRem = base.getMaxDamage() - base.getDamageValue();
            int prevRem = preview.getMaxDamage() - preview.getDamageValue();
            int d = prevRem - baseRem;
            if (d != 0) out.add(coloredDelta("Durability (remaining)", d));
        }

        RuneStats bStats = RuneStats.get(base);
        RuneStats pStats = RuneStats.get(preview);

        Map<RuneStatType, Float> bm = bStats == null ? Map.of() : bStats.view();
        Map<RuneStatType, Float> pm = pStats == null ? Map.of() : pStats.view();

        List<RuneStatType> keys = new ArrayList<>();
        keys.addAll(bm.keySet());
        for (RuneStatType t : pm.keySet()) if (!keys.contains(t)) keys.add(t);
        keys.sort(Comparator.comparing(RuneStatType::id));

        List<Component> baseTooltip = this.getTooltipFromItem(this.minecraft, base);
        double baseAttackDamage = 1.0 + sumAttrFromTooltip(baseTooltip, ATTR_ATTACK_DAMAGE);
        double baseAttackSpeed = 4.0 + sumAttrFromTooltip(baseTooltip, ATTR_ATTACK_SPEED);
        int baseMaxDur = base.isDamageableItem() ? base.getMaxDamage() : 0;

        for (RuneStatType t : keys) {
            float bv = bm.getOrDefault(t, 0.0F);
            float pv = pm.getOrDefault(t, 0.0F);
            float dvRaw = pv - bv;
            if (Math.abs(dvRaw) <= 0.0001F) continue;

            String id = t.id();

            if ("attack_damage".equals(id)) {
                double pct = pctFraction(dvRaw);
                out.add(coloredDelta("Attack damage", baseAttackDamage * pct));
                continue;
            }

            if ("attack_speed".equals(id)) {
                double pct = pctFraction(dvRaw);
                out.add(coloredDelta("Attack speed", baseAttackSpeed * pct));
                continue;
            }

            if ("durability".equals(id) && baseMaxDur > 0) {
                double pct = pctFraction(dvRaw);
                out.add(coloredDelta("Durability", baseMaxDur * pct));
                continue;
            }

            out.add(coloredPercent(pretty(id), dvRaw));
        }

        var be = base.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        var pe = preview.getOrDefault(net.minecraft.core.component.DataComponents.ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);

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

    private static double sumAttrFromTooltip(List<Component> tooltip, String attrKey) {
        double sum = 0.0;

        for (Component line : tooltip) {
            if (!(line.getContents() instanceof TranslatableContents tc)) continue;

            String key = tc.getKey();
            if (key == null) continue;

            boolean isPlus = key.startsWith("attribute.modifier.plus");
            boolean isTake = key.startsWith("attribute.modifier.take");
            boolean isEq = key.startsWith("attribute.modifier.equals");
            if (!isPlus && !isTake && !isEq) continue;

            Object[] args = tc.getArgs();
            if (args == null || args.length < 2) continue;

            Object a0 = args[0];
            Object a1 = args[1];

            if (!(a1 instanceof Component attrComp)) continue;
            if (!(attrComp.getContents() instanceof TranslatableContents atc)) continue;
            if (!attrKey.equals(atc.getKey())) continue;

            double amt = 0.0;
            if (a0 instanceof Number n) amt = n.doubleValue();
            else if (a0 instanceof Component c) amt = parseDoubleSafe(c.getString());
            else if (a0 instanceof String s) amt = parseDoubleSafe(s);

            if (isTake) amt = -Math.abs(amt);
            sum += amt;
        }

        return sum;
    }

    private static double parseDoubleSafe(String s) {
        if (s == null) return 0.0;
        String t = s.replace(",", "").replace("%", "").trim();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < t.length(); i++) {
            char ch = t.charAt(i);
            if ((ch >= '0' && ch <= '9') || ch == '.' || ch == '-' || ch == '+') b.append(ch);
        }
        try {
            return Double.parseDouble(b.toString());
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    private static double pctFraction(float dvRaw) {
        double v = dvRaw;
        if (Math.abs(v) > 2.0) v = v / 100.0;
        return v;
    }

    private static Map<String, Integer> enchMap(net.minecraft.world.item.enchantment.ItemEnchantments e) {
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

    private static Component coloredDelta(String label, double delta) {
        ChatFormatting fmt = delta > 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        String s = formatSigned(delta);
        return Component.literal(label + ": " + s).withStyle(fmt);
    }

    private static Component coloredPercent(String label, float dvRaw) {
        double pct = dvRaw;
        if (Math.abs(pct) <= 2.0) pct = pct * 100.0;
        ChatFormatting fmt = pct > 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        String s = (pct > 0 ? "+" : "") + trimDouble(pct) + "%";
        return Component.literal(label + ": " + s).withStyle(fmt);
    }

    private static String formatSigned(double v) {
        String s = trimDouble(v);
        if (v > 0) s = "+" + s;
        return s;
    }

    private static String trimDouble(double v) {
        double av = Math.abs(v);
        if (av >= 1000.0) return String.format(java.util.Locale.ROOT, "%.0f", v);
        if (av >= 100.0) return String.format(java.util.Locale.ROOT, "%.1f", v);
        if (av >= 10.0) return String.format(java.util.Locale.ROOT, "%.2f", v);
        if (av >= 1.0) return String.format(java.util.Locale.ROOT, "%.2f", v);
        if (av >= 0.1) return String.format(java.util.Locale.ROOT, "%.2f", v);
        return String.format(java.util.Locale.ROOT, "%.3f", v);
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
