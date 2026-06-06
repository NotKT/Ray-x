package com.xraymod.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class XRayConfig {
    public static XRayConfig instance = new XRayConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("xraymod.json");

    // Block XRay
    public List<String> visibleBlocks = new ArrayList<>();

    // Entity Glow
    public List<String> glowEntities = new ArrayList<>();

    // Ranges
    public int chunkRange = 6;
    public int entityGlowRange = 6;

    // Toggles
    public boolean fullbright = false;
    public boolean xrayToggleMode = false;
    public boolean entityGlowToggleMode = false;

    // Utility
    public UtilityConfig utility = new UtilityConfig();

    public static void load() {
        if (CONFIG_PATH.toFile().exists()) {
            try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                XRayConfig loaded = GSON.fromJson(reader, XRayConfig.class);
                if (loaded != null) {
                    instance = loaded;
                    if (instance.utility == null) instance.utility = new UtilityConfig();
                }
            } catch (Exception e) {
                instance = new XRayConfig();
            }
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(instance, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
