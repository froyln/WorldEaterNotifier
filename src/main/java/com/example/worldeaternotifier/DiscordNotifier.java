package com.example.worldeaternotifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DiscordNotifier {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static ModConfig config;

    public static void setConfig(ModConfig config) {
        DiscordNotifier.config = config;
    }

    public static void sendStart(String worldEaterName) {
        send("<@&" + config.roleId + "> WorldEater **'" + worldEaterName + "'** has started.");
    }

    public static void sendStuck(String worldEaterName) {
        send("<@&" + config.roleId + "> WorldEater **'" + worldEaterName + "'** has stopped due to an obstruction.");
    }

    public static void sendResumed(String worldEaterName) {
        send("<@&" + config.roleId + "> WorldEater **'" + worldEaterName + "'** has started again.");
    }

    public static void sendManuallyStopped(String worldEaterName) {
        send("<@&" + config.roleId + "> WorldEater **'" + worldEaterName + "'** was stopped manually.");
    }

    private static void send(String content) {
        if (config == null || config.webhookUrl.isBlank()) {
            return;
        }
        try {
            String json = "{\"content\":\"" + content.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}