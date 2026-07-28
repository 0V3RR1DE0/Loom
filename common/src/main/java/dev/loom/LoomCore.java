package dev.loom;

import dev.architectury.event.events.common.PlayerEvent;
import dev.loom.util.LoomConfigInitializer;  // Import the utility class
import net.minecraft.network.chat.Component;
import static dev.loom.util.Log.*;
import dev.architectury.platform.Platform;

public final class LoomCore {
    public static final String MOD_ID = "loom";

    public static void init() {
        printBanner();

        // Initialize command
        LoomCommand.register();
        info("Commands registered.");

        // Initialize config structure using the utility class
        LoomConfigInitializer.initialize();

        // Register event listeners
        registerEventListeners();

        // Log success
        info("Loom initialized successfully!");
        info("Config directory: {}", Platform.getConfigFolder());
    }

    private static void printBanner() {
        info("");
        info("+-------------------------------------------+");
        info("|              L O O M                      |");
        info("|    A modern scripting engine              |");
        info("|    for Fabric & NeoForge                  |");
        info("|    Version 0.1.0                          |");
        info("+-------------------------------------------+");
        info("");
    }

    private static void registerEventListeners() {
        PlayerEvent.PLAYER_JOIN.register(player -> {
            player.sendSystemMessage(Component.literal("Welcome to Loom!"));
        });
        info("Event listeners registered.");
    }
}