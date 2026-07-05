package com.example.worldeaternotifier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WorldEaterCommand {

    // Proveedor de sugerencias para los nombres de world eaters
    private static final SuggestionProvider<ServerCommandSource> WORLD_EATER_NAMES = (context, builder) -> {
        String[] names = WorldEaterManager.getInstance().getAllNames();
        return CommandSource.suggestMatching(names, builder);
    };

    // Proveedor de sugerencias para las coordenadas: bloque al que el jugador está mirando
    private static final SuggestionProvider<ServerCommandSource> BLOCK_TARGET_COORDINATE = (context, builder) -> {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return Suggestions.empty();
        }
        // Raycast de 5 bloques de alcance
        HitResult hit = player.raycast(5.0, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            // Determinar qué coordenada se está solicitando viendo el nombre del argumento
            String argName = builder.getRemaining().isEmpty() ? "" : context.getNodes().get(context.getNodes().size() - 1).getNode().getName();
            int value = 0;
            switch (argName) {
                case "x1":
                case "x2":
                    value = pos.getX();
                    break;
                case "y1":
                case "y2":
                    value = pos.getY();
                    break;
                case "z1":
                case "z2":
                    value = pos.getZ();
                    break;
                default:
                    return Suggestions.empty();
            }
            builder.suggest(value);
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("worldeater")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("create")
                        .then(argument("name", StringArgumentType.word())
                                .then(argument("x1", IntegerArgumentType.integer())
                                        .suggests(BLOCK_TARGET_COORDINATE)
                                        .then(argument("y1", IntegerArgumentType.integer())
                                                .suggests(BLOCK_TARGET_COORDINATE)
                                                .then(argument("z1", IntegerArgumentType.integer())
                                                        .suggests(BLOCK_TARGET_COORDINATE)
                                                        .then(argument("x2", IntegerArgumentType.integer())
                                                                .suggests(BLOCK_TARGET_COORDINATE)
                                                                .then(argument("y2", IntegerArgumentType.integer())
                                                                        .suggests(BLOCK_TARGET_COORDINATE)
                                                                        .then(argument("z2", IntegerArgumentType.integer())
                                                                                .suggests(BLOCK_TARGET_COORDINATE)
                                                                                .executes(WorldEaterCommand::executeCreate)
                                                                        ))))))))
                .then(literal("start")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(WORLD_EATER_NAMES)
                                .executes(WorldEaterCommand::executeStart)))
                .then(literal("stop")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(WORLD_EATER_NAMES)
                                .executes(WorldEaterCommand::executeStop)))
                .then(literal("list")
                        .executes(WorldEaterCommand::executeList))
                .then(literal("delete")
                        .then(argument("name", StringArgumentType.word())
                                .suggests(WORLD_EATER_NAMES)
                                .executes(WorldEaterCommand::executeDelete)))
        );
    }

    private static int executeCreate(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        int x1 = IntegerArgumentType.getInteger(ctx, "x1");
        int y1 = IntegerArgumentType.getInteger(ctx, "y1");
        int z1 = IntegerArgumentType.getInteger(ctx, "z1");
        int x2 = IntegerArgumentType.getInteger(ctx, "x2");
        int y2 = IntegerArgumentType.getInteger(ctx, "y2");
        int z2 = IntegerArgumentType.getInteger(ctx, "z2");

        World world = ctx.getSource().getWorld();
        RegistryKey<World> dimension = world.getRegistryKey();

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        WorldEaterDefinition definition = new WorldEaterDefinition(name, minX, minY, minZ, maxX, maxY, maxZ, dimension);
        boolean success = WorldEaterManager.getInstance().create(definition);
        if (success) {
            ctx.getSource().sendFeedback(() -> Text.literal("World eater '" + name + "' created."), true);
        } else {
            ctx.getSource().sendError(Text.literal("A world eater with name '" + name + "' already exists."));
        }
        return 1;
    }

    private static int executeStart(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        boolean success = WorldEaterManager.getInstance().start(name);
        if (success) {
            ctx.getSource().sendFeedback(() -> Text.literal("World eater '" + name + "' started."), true);
            DiscordNotifier.sendStart(name);
        } else {
            ctx.getSource().sendError(Text.literal("No world eater named '" + name + "'."));
        }
        return 1;
    }

    private static int executeStop(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        boolean success = WorldEaterManager.getInstance().stop(name);
        if (success) {
            ctx.getSource().sendFeedback(() -> Text.literal("World eater '" + name + "' stopped."), true);
            DiscordNotifier.sendManuallyStopped(name);
        } else {
            ctx.getSource().sendError(Text.literal("No world eater named '" + name + "'."));
        }
        return 1;
    }

    private static int executeList(CommandContext<ServerCommandSource> ctx) {
        var instances = WorldEaterManager.getInstance().getAll();
        if (instances.isEmpty()) {
            ctx.getSource().sendFeedback(() -> Text.literal("No world eaters defined."), false);
            return 1;
        }
        for (WorldEaterInstance inst : instances) {
            String status = inst.isActive() ? "active" : "inactive";
            ctx.getSource().sendFeedback(() -> Text.literal("- " + inst.getDefinition().name() + " (" + status + ")"), false);
        }
        return 1;
    }

    private static int executeDelete(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        WorldEaterInstance inst = WorldEaterManager.getInstance().get(name);
        if (inst == null) {
            ctx.getSource().sendError(Text.literal("No world eater named '" + name + "'."));
            return 0;
        }
        inst.stop(); // Detener sin notificación (se borra)
        WorldEaterManager.getInstance().delete(name);
        ctx.getSource().sendFeedback(() -> Text.literal("World eater '" + name + "' deleted."), true);
        return 1;
    }
}