package net.revilodev.runic.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.screen.custom.EtchingTableMenu;

import java.util.Optional;

public record PlaceEtchingRecipePayload(ResourceLocation recipeId) implements CustomPacketPayload {
    public static final Type<PlaceEtchingRecipePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "place_etching_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceEtchingRecipePayload> STREAM_CODEC =
            StreamCodec.composite(ResourceLocation.STREAM_CODEC, PlaceEtchingRecipePayload::recipeId, PlaceEtchingRecipePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlaceEtchingRecipePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            if (!(sp.containerMenu instanceof EtchingTableMenu menu)) return;

            Optional<? extends net.minecraft.world.item.crafting.RecipeHolder<?>> any = sp.level().getRecipeManager().byKey(payload.recipeId());
            if (any.isEmpty()) return;

            net.minecraft.world.item.crafting.RecipeHolder<?> h = any.get();
            if (!(h.value() instanceof EtchingTableRecipe)) return;

            @SuppressWarnings("unchecked")
            net.minecraft.world.item.crafting.RecipeHolder<EtchingTableRecipe> typed = (net.minecraft.world.item.crafting.RecipeHolder<EtchingTableRecipe>) h;

            menu.placeRecipeFromBook(sp, typed);
        });
    }
}
