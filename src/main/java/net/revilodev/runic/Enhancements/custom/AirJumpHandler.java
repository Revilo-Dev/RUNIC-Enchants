package net.revilodev.runic.Enhancements.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.revilodev.runic.Enhancements.ModEnhancements;
import net.revilodev.runic.RunicMod;

public final class AirJumpHandler {
    private static final String KEY_STATE = "runic.air_jump.state";
    private static final String KEY_PREV_SNEAK = "runic.air_jump.prev_sneak";
    private static final String KEY_USED = "runic.air_jump.used";

    public static void register() {
        NeoForge.EVENT_BUS.addListener(AirJumpHandler::onPlayerTick);
        RunicMod.LOGGER.info("[Runic] AirJumpHandler registered");
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) player.level();

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty()) return;

        //Holder<Enchantment> enchant = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnhancements.AIR_JUMP);
        //int lvl = EnchantmentHelper.getItemEnchantmentLevel(enchant, boots);
        //if (lvl <= 0) return;

        boolean onGround = player.onGround();
        boolean sneaking = player.isShiftKeyDown();
        int prevSneak = player.getPersistentData().getInt(KEY_PREV_SNEAK);
        boolean justPressed = sneaking && prevSneak == 0;
        boolean justReleased = !sneaking && prevSneak == 1;
        player.getPersistentData().putInt(KEY_PREV_SNEAK, sneaking ? 1 : 0);

        if (onGround) {
            if (player.getPersistentData().getInt(KEY_STATE) != 0 || player.getPersistentData().getInt(KEY_USED) != 0) {
                RunicMod.LOGGER.info("[Runic] AirJump reset on ground for {}", player.getGameProfile().getName());
            }
            player.getPersistentData().putInt(KEY_STATE, 0);
            player.getPersistentData().putInt(KEY_USED, 0);
            return;
        }

        int state = player.getPersistentData().getInt(KEY_STATE);
        int used = player.getPersistentData().getInt(KEY_USED);
        boolean falling = player.getDeltaMovement().y < 0.0D;
        boolean airBelow = level.getBlockState(player.blockPosition().below()).isAir();

        if (state == 0 && justPressed) {
            player.getPersistentData().putInt(KEY_STATE, 1);
            RunicMod.LOGGER.info("[Runic] AirJump state=1 first sneak press {}", player.getGameProfile().getName());
            return;
        }

        if (state == 1 && justReleased) {
            player.getPersistentData().putInt(KEY_STATE, 2);
            RunicMod.LOGGER.info("[Runic] AirJump state=2 released {}", player.getGameProfile().getName());
            return;
        }

        if (state == 2 && justPressed && used == 0 && falling && airBelow) {
            player.getPersistentData().putInt(KEY_USED, 1);
            player.getPersistentData().putInt(KEY_STATE, 3);
            player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 3, true, false, false));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.0F, 1.0F);
            RunicMod.LOGGER.info("[Runic] AirJump levitation applied to {}", player.getGameProfile().getName());
            return;
        }

        if (player.getPersistentData().getInt(KEY_STATE) == 3 && !airBelow) {
            player.getPersistentData().putInt(KEY_STATE, 0);
            player.getPersistentData().putInt(KEY_USED, 0);
            RunicMod.LOGGER.info("[Runic] AirJump reset after landing {}", player.getGameProfile().getName());
        }
    }
}
