package com.example.worldeaternotifier;

public class WorldEaterInstance {
    private final WorldEaterDefinition definition;
    private boolean active;
    private int zeroTntChecks = 0;           // number of consecutive checks with 0 TNT
    private boolean stuckAlertSent = false;  // avoids duplicate stuck messages

    public WorldEaterInstance(WorldEaterDefinition definition) {
        this.definition = definition;
        this.active = false;
    }

    public WorldEaterDefinition getDefinition() {
        return definition;
    }

    public boolean isActive() {
        return active;
    }

    public void start() {
        this.active = true;
        this.zeroTntChecks = 0;
        this.stuckAlertSent = false;
    }

    public void stop() {
        this.active = false;
        this.zeroTntChecks = 0;
        this.stuckAlertSent = false;
    }

    public int getZeroTntChecks() {
        return zeroTntChecks;
    }

    public void incrementZeroTntChecks() {
        zeroTntChecks++;
    }

    public void resetZeroTntChecks() {
        zeroTntChecks = 0;
        if (stuckAlertSent) {
            // World eater just resumed after being stuck – notify
            DiscordNotifier.sendResumed(definition.name());
            stuckAlertSent = false;
        }
    }

    public boolean isStuckAlertSent() {
        return stuckAlertSent;
    }

    public void markStuckAlertSent() {
        this.stuckAlertSent = true;
    }
}