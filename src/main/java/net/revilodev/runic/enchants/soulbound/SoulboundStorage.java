package net.revilodev.runic.enchants.soulbound;

import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SoulboundStorage {
    private static final Map<UUID, List<ItemStack>> MAP = new ConcurrentHashMap<>();

    public static void add(UUID id, ItemStack stack) {
        MAP.computeIfAbsent(id, k -> new ArrayList<>()).add(stack.copy());
    }

    public static List<ItemStack> take(UUID id) {
        List<ItemStack> list = MAP.remove(id);
        return list == null ? Collections.emptyList() : list;
    }

    private SoulboundStorage() {}
}
