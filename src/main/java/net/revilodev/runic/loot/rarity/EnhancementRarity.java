package net.revilodev.runic.loot.rarity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public enum EnhancementRarity {
    COMMON("common", ChatFormatting.GRAY, 20, 0),
    UNCOMMON("uncommon", ChatFormatting.GREEN, 12, 1),
    RARE("rare", ChatFormatting.DARK_AQUA, 6, 2),
    EPIC("epic", ChatFormatting.GOLD, 3, 3),
    LEGENDARY("legendary", ChatFormatting.LIGHT_PURPLE, 1, 4),
    CURSED("cursed", ChatFormatting.DARK_RED, 2, 0);

    private final String key;
    private final ChatFormatting color;
    private final int weight;
    private final int stars;

    EnhancementRarity(String key, ChatFormatting color, int weight, int stars) {
        this.key = key;
        this.color = color;
        this.weight = weight;
        this.stars = stars;
    }

    public String key() { return key; }
    public ChatFormatting color() { return color; }
    public int weight() { return weight; }
    public int stars() { return stars; }

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
