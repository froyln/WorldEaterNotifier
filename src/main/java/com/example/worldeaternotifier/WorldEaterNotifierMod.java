package com.example.worldeaternotifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class WorldEaterNotifierMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ModConfig config = ModConfig.load();
        DiscordNotifier.setConfig(config);

        // Cargar world eaters guardadas en memoria
        WorldEaterManager manager = WorldEaterManager.getInstance();
        manager.setConfig(config);
        for (ModConfig.SavedWorldEater saved : config.worldEaters) {
            RegistryKey<World> dimensionKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(saved.dimension));
            WorldEaterDefinition definition = new WorldEaterDefinition(
                    saved.name,
                    saved.minX, saved.minY, saved.minZ,
                    saved.maxX, saved.maxY, saved.maxZ,
                    dimensionKey
            );
            WorldEaterInstance instance = new WorldEaterInstance(definition);
            if (saved.active) {
                instance.start(); // activa la monitorización
            }
            // Insertar manualmente en el mapa del manager (necesitamos un método de carga)
            // Añadimos un método en WorldEaterManager para cargar instancias directamente
            manager.loadInstance(instance);
        }

        WorldEaterCheckHandler.register();
        CommandRegistrationCallback.EVENT.register(WorldEaterCommand::register);
    }
}