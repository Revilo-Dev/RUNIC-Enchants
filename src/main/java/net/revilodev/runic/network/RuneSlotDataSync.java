package net.revilodev.runic.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.runes.RuneSlotCapacityData;

import java.util.HashMap;
import java.util.Map;

public record RuneSlotDataSync(Map<ResourceLocation,Integer> items,
                               Map<String,Integer> defaults) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "rune_slots");
    public static final Type<RuneSlotDataSync> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RuneSlotDataSync> CODEC =
            new StreamCodec<>() {
                @Override
                public RuneSlotDataSync decode(RegistryFriendlyByteBuf buf) {
                    int count = buf.readVarInt();
                    Map<ResourceLocation,Integer> items = new HashMap<>();
                    for (int i=0;i<count;i++) {
                        items.put(ResourceLocation.parse(buf.readUtf()), buf.readVarInt());
                    }
                    int dCount = buf.readVarInt();
                    Map<String,Integer> defs = new HashMap<>();
                    for (int i=0;i<dCount;i++) {
                        defs.put(buf.readUtf(), buf.readVarInt());
                    }
                    return new RuneSlotDataSync(items, defs);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, RuneSlotDataSync msg) {
                    buf.writeVarInt(msg.items().size());
                    msg.items().forEach((id,v)->{
                        buf.writeUtf(id.toString());
                        buf.writeVarInt(v);
                    });
                    buf.writeVarInt(msg.defaults().size());
                    msg.defaults().forEach((k,v)->{
                        buf.writeUtf(k);
                        buf.writeVarInt(v);
                    });
                }
            };

    public static final IPayloadHandler<RuneSlotDataSync> HANDLER = (msg, ctx) -> {
        if (Minecraft.getInstance().level != null) {
            RuneSlotCapacityData.importFromNetwork(msg.items(), msg.defaults());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
