package net.revilodev.runic.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.effect.ModMobEffects;

import java.util.Optional;

public class StunningOverlay {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/gui/stunned_overlay.png");
    private static Holder<?> CACHED_STUN_HOLDER = null;

    public static void register() {
        NeoForge.EVENT_BUS.register(StunningOverlay.class);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (CACHED_STUN_HOLDER == null) {
            Optional<? extends Holder<?>> opt = mc.player.registryAccess()
                    .registryOrThrow(Registries.MOB_EFFECT)
                    .getHolder(ModMobEffects.STUNNING.getKey());
            if (opt.isEmpty()) return;
            CACHED_STUN_HOLDER = opt.get();
        }

        if (!mc.player.hasEffect((Holder) CACHED_STUN_HOLDER)) return;

        GuiGraphics g = event.getGuiGraphics();
        RenderSystem.enableBlend();

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        g.blit(OVERLAY, 0, 0, 0, 0, w, h, 256, 256);

        RenderSystem.disableBlend();
    }
}
