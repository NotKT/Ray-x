package com.xraymod.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    private Set<String> visibleBlocks = new HashSet<>(DEFAULT_VISIBLE);

    public static final Set<String> DEFAULT_VISIBLE = Set.of(
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

    public Set<String> getVisibleBlocks() { return visibleBlocks; }
    public boolean isVisible(String blockId) { return visibleBlocks.contains(blockId); }
    public void addBlock(String blockId) { visibleBlocks.add(blockId); save(); }
    public void removeBlock(String blockId) { visibleBlocks.remove(blockId); save(); }
    public void resetToDefaults() { visibleBlocks = new HashSet<>(DEFAULT_VISIBLE); save(); }

    public void save() {
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(visibleBlocks, w);
        } catch (IOException e) {
            System.err.println("[XRayMod] Save failed: " + e.getMessage());
        }
    }

    public static XRayConfig load() {
        XRayConfig cfg = new XRayConfig();
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) { cfg.save(); return cfg; }
        try (Reader r = new FileReader(file)) {
            Type type = new TypeToken<HashSet<String>>() {}.getType();
            Set<String> loaded = GSON.fromJson(r, type);
            if (loaded != null) cfg.visibleBlocks = loaded;
        } catch (IOException e) {
            System.err.println("[XRayMod] Load failed: " + e.getMessage());
        }
        return cfg;
    }
}
