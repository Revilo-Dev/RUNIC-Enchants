package net.revilodev.runic.Enhancements.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.revilodev.runic.Enhancements.ModEnhancements;

import java.util.ArrayList;
import java.util.List;

public final class SoulboundHandler {
    private static final String KEY = "runic_soulbound";

    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Holder<Enchantment> holder = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(ModEnhancements.SOULBOUND);
        List<CompoundTag> keep = new ArrayList<>();
        var it = event.getDrops().iterator();
        while (it.hasNext()) {
            ItemEntity e = it.next();
            ItemStack s = e.getItem();
            if (EnchantmentHelper.getItemEnchantmentLevel(holder, s) > 0) {
                CompoundTag t = new CompoundTag();
                s.save(player.registryAccess(), t);
                keep.add(t);
                it.remove();
            }
        }
        if (!keep.isEmpty()) {
            CompoundTag store = new CompoundTag();
            for (int i = 0; i < keep.size(); i++) store.put("i" + i, keep.get(i));
            player.getPersistentData().put(KEY, store);
        }
    }

    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag store = event.getOriginal().getPersistentData().getCompound(KEY);
        if (store.isEmpty()) return;
        for (String k : store.getAllKeys()) {
            ItemStack s = ItemStack.parseOptional(player.registryAccess(), store.getCompound(k));
            if (!s.isEmpty() && s.isDamageableItem()) {
                int max = s.getMaxDamage();
                int add = Math.max(1, Mth.floor(max * 0.25f));
                int dmg = s.getDamageValue() + add;
                if (dmg >= max) continue;
                s.setDamageValue(dmg);
            }
            if (!s.isEmpty() && !player.addItem(s)) player.drop(s, true);
        }
        player.getPersistentData().remove(KEY);
    }
}
