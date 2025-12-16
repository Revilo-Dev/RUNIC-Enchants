package net.revilodev.runic.loot.rarity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class EnhancementRarityClientReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    public static final String FOLDER = "rarities";

    public EnhancementRarityClientReloadListener() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects,
                         ResourceManager rm,
                         ProfilerFiller profiler) {

        EnhancementRarities.clear();

        Map<ResourceLocation, EnhancementRarity> tmp = new HashMap<>();
        EnhancementRarity def = EnhancementRarity.COMMON;

        for (JsonElement el : objects.values()) {
            var root = el.getAsJsonObject();

            if (root.has("default")) {
                def = EnhancementRarity.fromKey(
                        GsonHelper.getAsString(root, "default"),
                        EnhancementRarity.COMMON
                );
            }

            if (!root.has("entries")) continue;

            var entries = root.getAsJsonObject("entries");
            for (var e : entries.entrySet()) {
                ResourceLocation id = ResourceLocation.tryParse(e.getKey());
                if (id == null) continue;

                EnhancementRarity r =
                        EnhancementRarity.fromKey(
                                GsonHelper.convertToString(e.getValue(), "rarity"),
                                null
                        );

                if (r != null) tmp.put(id, r);
            }
        }

        EnhancementRarities.replaceAllClient(tmp, def);
    }
}
