package com.example.worldeaternotifier;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.List;

public class WorldEaterCheckHandler {
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(WorldEaterCheckHandler::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        tickCounter++;
        if (tickCounter % 40 != 0) return;

        WorldEaterManager manager = WorldEaterManager.getInstance();
        for (WorldEaterInstance instance : manager.getAll()) {
            WorldEaterDefinition def = instance.getDefinition();
            if (!instance.isActive()) continue;
            if (!def.dimension().equals(world.getRegistryKey())) continue;

            Box box = new Box(def.minX(), def.minY(), def.minZ(),
                    def.maxX() + 1, def.maxY() + 1, def.maxZ() + 1);

            List<TntEntity> tntList = world.getEntitiesByType(EntityType.TNT, box, tnt -> true);

            // Se considera en marcha si hay al menos 3 TNT encendidas
            if (tntList.size() < 3) {
                instance.incrementZeroTntChecks();
                if (instance.getZeroTntChecks() >= 60 && !instance.isStuckAlertSent()) {
                    instance.markStuckAlertSent();
                    DiscordNotifier.sendStuck(def.name());
                }
            } else {
                instance.resetZeroTntChecks();
            }
        }
    }
}