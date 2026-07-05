package com.example.worldeaternotifier;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldEaterManager {
    private static final WorldEaterManager INSTANCE = new WorldEaterManager();
    private final Map<String, WorldEaterInstance> instances = new ConcurrentHashMap<>();
    private ModConfig config;

    private WorldEaterManager() {}

    public static WorldEaterManager getInstance() {
        return INSTANCE;
    }

    public void setConfig(ModConfig config) {
        this.config = config;
    }

    public boolean create(WorldEaterDefinition definition) {
        String name = definition.name();
        if (instances.containsKey(name)) {
            return false;
        }
        WorldEaterInstance instance = new WorldEaterInstance(definition);
        instances.put(name, instance);

        // Persistir
        ModConfig.SavedWorldEater saved = new ModConfig.SavedWorldEater(
                name,
                definition.minX(), definition.minY(), definition.minZ(),
                definition.maxX(), definition.maxY(), definition.maxZ(),
                definition.dimension().getValue().toString(),
                false // inicia detenida
        );
        config.worldEaters.add(saved);
        config.save();
        return true;
    }

    public boolean start(String name) {
        WorldEaterInstance instance = instances.get(name);
        if (instance == null) return false;
        instance.start();
        updateSavedState(name, true);
        return true;
    }

    public boolean stop(String name) {
        WorldEaterInstance instance = instances.get(name);
        if (instance == null) return false;
        instance.stop();
        updateSavedState(name, false);
        return true;
    }

    public boolean delete(String name) {
        WorldEaterInstance removed = instances.remove(name);
        if (removed == null) return false;
        config.worldEaters.removeIf(we -> we.name.equals(name));
        config.save();
        return true;
    }

    public Collection<WorldEaterInstance> getAll() {
        return instances.values();
    }

    public WorldEaterInstance get(String name) {
        return instances.get(name);
    }

    private void updateSavedState(String name, boolean active) {
        for (ModConfig.SavedWorldEater saved : config.worldEaters) {
            if (saved.name.equals(name)) {
                saved.active = active;
                config.save();
                return;
            }
        }
    }

    public String[] getAllNames() {
        return instances.keySet().toArray(new String[0]);
    }

    public void loadInstance(WorldEaterInstance instance) {
        instances.put(instance.getDefinition().name(), instance);
    }
}