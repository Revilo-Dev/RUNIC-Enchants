package net.revilodev.runic.enchants;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.revilodev.runic.RunicMod;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public class ModEnchantmentEvents {

    private static final ResourceKey<Enchantment> POISON_CLOUD_KEY =
            ResourceKey.create(Registries.ENCHANTMENT, new ResourceLocation(RunicMod.MOD_ID, "poison_cloud"));
    private static final ResourceKey<Enchantment> PAIN_CYCLE_KEY =
            ResourceKey.create(Registries.ENCHANTMENT, new ResourceLocation(RunicMod.MOD_ID, "pain_cycle"));
    private static final ResourceKey<Enchantment> COMMITTED_KEY =
            ResourceKey.create(Registries.ENCHANTMENT, new ResourceLocation(RunicMod.MOD_ID, "committed"));

    @SubscribeEvent
    public static void onCrit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (player == null) return;

        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) return;

        ItemEnchantments ench = weapon.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        int lvl = getLevel(ench, player.level().registryAccess(), PAIN_CYCLE_KEY);

        if (lvl > 0 && event.isVanillaCritical()) {
            player.hurt(player.damageSources().generic(), 2.0F);
            player.getPersistentData().putBoolean("runic_pain_cycle_crit", true);
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        Entity src = event.getSource().getEntity();
        if (!(src instanceof LivingEntity attacker)) return;

        if (attacker.getPersistentData().getBoolean("runic_pain_cycle_crit")) {
            event.setAmount(event.getAmount() * 3.0F);
            attacker.getPersistentData().remove("runic_pain_cycle_crit");
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) return;

        ItemEnchantments ench = weapon.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        RegistryAccess access = attacker.level().registryAccess();

        int poisonLvl = getLevel(ench, access, POISON_CLOUD_KEY);
        int committedLvl = getLevel(ench, access, COMMITTED_KEY);

        if (poisonLvl > 0 && attacker.getRandom().nextFloat() < 0.25F) {
            LivingEntity target = event.getEntity();

            AreaEffectCloud cloud = new AreaEffectCloud(target.level(), target.getX(), target.getY(), target.getZ());
            cloud.setRadius(3.0F);
            cloud.setDuration(40);
            cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            cloud.setOwner(attacker);

            target.level().addFreshEntity(cloud);
        }

        if (committedLvl > 0) {
            LivingEntity target = event.getEntity();
            float ratio = target.getHealth() / target.getMaxHealth();

            if (ratio < 0.75F) {
                event.setAmount(event.getAmount() * 1.35F);
            }
        }
    }

    private static int getLevel(ItemEnchantments ench, RegistryAccess access, ResourceKey<Enchantment> key) {
        return access.registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(key)
                .map(ench::getLevel)
                .orElse(0);
    }
}
