package net.revilodev.runic.loot.rarity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public enum EnhancementRarity {
    COMMON("common", ChatFormatting.GRAY, 20),
    UNCOMMON("uncommon", ChatFormatting.GREEN, 12),
    RARE("rare", ChatFormatting.DARK_AQUA, 6),
    EPIC("epic", ChatFormatting.GOLD, 3),
    LEGENDARY("legendary", ChatFormatting.LIGHT_PURPLE, 1),
    CURSED("cursed", ChatFormatting.DARK_RED, 2);

    private final String key;
    private final ChatFormatting color;
    private final int weight;

    EnhancementRarity(String key, ChatFormatting color, int weight) {
        this.key = key;
        this.color = color;
        this.weight = weight;
    }

    public String key() { return key; }
    public ChatFormatting color() { return color; }
    public int weight() { return weight; }

    public Style style() {
        return Style.EMPTY.withColor(color);
    }

    public MutableComponent applyTo(Component base) {
        return base.copy().withStyle(style());
    }

    public static EnhancementRarity fromKey(String s, EnhancementRarity fallback) {
        if (s == null) return fallback;
        String n = s.trim().toLowerCase();
        for (EnhancementRarity r : values()) {
            if (r.key.equals(n)) return r;
        }
        return fallback;
    }
}
