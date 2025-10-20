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

    public EnhancementRarityClientReloadListener() { super(GSON, FOLDER); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager rm, ProfilerFiller profiler) {
        EnhancementRarities.clear();
        Map<ResourceLocation, EnhancementRarity> tmp = new HashMap<>();
        EnhancementRarity def = EnhancementRarity.COMMON;

        for (var e : objects.entrySet()) {
            var root = e.getValue().getAsJsonObject();
            var defStr = GsonHelper.getAsString(root, "default", null);
            if (defStr != null) def = EnhancementRarity.fromKey(defStr, EnhancementRarity.COMMON);
            if (root.has("entries")) {
                var obj = root.getAsJsonObject("entries");
                for (var ent : obj.entrySet()) {
                    var id = ResourceLocation.tryParse(ent.getKey());
                    if (id == null) continue;
                    var rar = EnhancementRarity.fromKey(GsonHelper.convertToString(ent.getValue(), "rarity"), null);
                    if (rar != null) tmp.put(id, rar);
                }
            }
        }
        EnhancementRarities.replaceAllClient(tmp, def);
    }
}
