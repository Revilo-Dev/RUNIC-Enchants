package net.revilodev.runic.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum GearAttribute {
    SEALED("sealed", Component.literal("Sealed"), ChatFormatting.WHITE),
    CURSED("cursed", Component.literal("Cursed"), ChatFormatting.DARK_RED),
    INSTABLE("instable", Component.literal("Instable"), ChatFormatting.WHITE),
    NEGATIVE("negative", Component.literal("Negative"), ChatFormatting.WHITE);

    private final String id;
    private final Component displayName;
    private final ChatFormatting color;

    GearAttribute(String id, Component displayName, ChatFormatting color) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public Component displayName() {
        return displayName;
    }

    public ChatFormatting color() {
        return color;
    }
}
