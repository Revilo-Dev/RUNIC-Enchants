package net.revilodev.runic.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.loot.rarity.EnhancementRarities;

import java.util.HashMap;
import java.util.Map;

public record EnhancementRarityDataSync(String defaultKey,
                                        Map<ResourceLocation, String> entries) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "enhancement_rarities");
    public static final Type<EnhancementRarityDataSync> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, EnhancementRarityDataSync> CODEC =
            new StreamCodec<>() {
                @Override
                public EnhancementRarityDataSync decode(RegistryFriendlyByteBuf buf) {
                    String defaultKey = buf.readUtf();
                    int count = buf.readVarInt();
                    Map<ResourceLocation, String> entries = new HashMap<>();
                    for (int i = 0; i < count; i++) {
                        ResourceLocation id = ResourceLocation.parse(buf.readUtf());
                        String rarityKey = buf.readUtf();
                        entries.put(id, rarityKey);
                    }
                    return new EnhancementRarityDataSync(defaultKey, entries);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, EnhancementRarityDataSync msg) {
                    buf.writeUtf(msg.defaultKey());
                    buf.writeVarInt(msg.entries().size());
                    msg.entries().forEach((id, rarityKey) -> {
                        buf.writeUtf(id.toString());
                        buf.writeUtf(rarityKey);
                    });
                }
            };

    public static final IPayloadHandler<EnhancementRarityDataSync> HANDLER = (msg, ctx) -> {
        if (Minecraft.getInstance().level != null) {
            EnhancementRarities.importFromNetwork(msg.entries(), msg.defaultKey());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
