package com.example.worldeaternotifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("worldeaternotifier.json");

    public String webhookUrl = "";
    public String roleId = "";
    public List<SavedWorldEater> worldEaters = new ArrayList<>();

    public static class SavedWorldEater {
        public String name;
        public int minX, minY, minZ;
        public int maxX, maxY, maxZ;
        public String dimension;
        public boolean active;

        public SavedWorldEater(String name, int minX, int minY, int minZ,
                               int maxX, int maxY, int maxZ, String dimension, boolean active) {
            this.name = name;
            this.minX = minX; this.minY = minY; this.minZ = minZ;
            this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
            this.dimension = dimension;
            this.active = active;
        }
    }

    public static ModConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                ModConfig config = GSON.fromJson(json, ModConfig.class);
                if (config.worldEaters == null) {
                    config.worldEaters = new ArrayList<>();
                }
                return config;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}