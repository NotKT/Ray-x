package com.xraymod.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class XRayConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("xraymod.json");

    private Set<String> visibleBlocks = new HashSet<>(DEFAULT_VISIBLE_BLOCKS);
    private Set<String> excludedEntities = new HashSet<>();
    private int chunkRange = 6;
    private boolean fullbright = false;

    public static final Set<String> DEFAULT_VISIBLE_BLOCKS = Set.of(
        "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
        "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
        "minecraft:gold_ore", "minecraft:deepslate_gold_ore",
        "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
        "minecraft:coal_ore", "minecraft:deepslate_coal_ore",
        "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore",
        "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore",
        "minecraft:copper_ore", "minecraft:deepslate_copper_ore",
        "minecraft:ancient_debris", "minecraft:chest",
        "minecraft:trapped_chest", "minecraft:ender_chest",
        "minecraft:barrel", "minecraft:spawner",
        "minecraft:trial_spawner", "minecraft:lava",
        "minecraft:water", "minecraft:bedrock"
    );

    // Block whitelist
    public Set<String> getVisibleBlocks() { return visibleBlocks; }
    public boolean isVisible(String blockId) { return visibleBlocks.contains(blockId); }
    public void addBlock(String blockId) { visibleBlocks.add(blockId); save(); }
    public void removeBlock(String blockId) { visibleBlocks.remove(blockId); save(); }

    // Entity exclusion list
    public Set<String> getExcludedEntities() { return excludedEntities; }
    public boolean isEntityExcluded(String entityId) { return excludedEntities.contains(entityId); }
    public void addExcludedEntity(String entityId) { excludedEntities.add(entityId); save(); }
    public void removeExcludedEntity(String entityId) { excludedEntities.remove(entityId); save(); }

    // Chunk range
    public int getChunkRange() { return chunkRange; }
    public void setChunkRange(int range) { this.chunkRange = Math.max(1, Math.min(32, range)); save(); }

    // Fullbright
    public boolean isFullbright() { return fullbright; }
    public void setFullbright(boolean fullbright) { this.fullbright = fullbright; save(); }

    public void resetToDefaults() {
        visibleBlocks = new HashSet<>(DEFAULT_VISIBLE_BLOCKS);
        excludedEntities = new HashSet<>();
        chunkRange = 6;
        fullbright = false;
        save();
    }

    public void save() {
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            JsonObject obj = new JsonObject();
            obj.add("visibleBlocks", GSON.toJsonTree(visibleBlocks));
            obj.add("excludedEntities", GSON.toJsonTree(excludedEntities));
            obj.addProperty("chunkRange", chunkRange);
            obj.addProperty("fullbright", fullbright);
            GSON.toJson(obj, w);
        } catch (IOException e) {
            System.err.println("[KPS+] Save failed: " + e.getMessage());
        }
    }

    public static XRayConfig load() {
        XRayConfig cfg = new XRayConfig();
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) { cfg.save(); return cfg; }
        try (Reader r = new FileReader(file)) {
            JsonObject obj = GSON.fromJson(r, JsonObject.class);
            if (obj.has("visibleBlocks")) {
                Type type = new TypeToken<HashSet<String>>() {}.getType();
                Set<String> loaded = GSON.fromJson(obj.get("visibleBlocks"), type);
                if (loaded != null) cfg.visibleBlocks = loaded;
            }
            if (obj.has("excludedEntities")) {
                Type type = new TypeToken<HashSet<String>>() {}.getType();
                Set<String> loaded = GSON.fromJson(obj.get("excludedEntities"), type);
                if (loaded != null) cfg.excludedEntities = loaded;
            }
            if (obj.has("chunkRange")) cfg.chunkRange = obj.get("chunkRange").getAsInt();
            if (obj.has("fullbright")) cfg.fullbright = obj.get("fullbright").getAsBoolean();
        } catch (IOException e) {
            System.err.println("[KPS+] Load failed: " + e.getMessage());
        }
        return cfg;
    }
}
