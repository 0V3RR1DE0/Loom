package dev.loom.util;

import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static dev.loom.util.Log.*;

public class LoomConfigInitializer {

    // Public method that other classes will call
    public static void initialize() {
        try {
            Path loomDir = Platform.getConfigFolder().resolve("loom");

            // Check if this is first launch
            boolean firstLaunch = Files.notExists(loomDir);

            info("Checking Loom config directory at {}...", loomDir.toAbsolutePath());

            // Create all directories
            createDirectories(loomDir);

            // Create config file
            createConfigFile(loomDir);

            // Create example script on first launch
            if (firstLaunch) {
                createWelcomeScript(loomDir);
            }

            info("Loom config structure verified.");

        } catch (IOException e) {
            error("Failed to initialize Loom config structure.", e);
        }
    }

    // Private helper methods
    private static void createDirectories(Path loomDir) throws IOException {
        debug("Creating directory structure...");
        Files.createDirectories(loomDir);
        Files.createDirectories(loomDir.resolve("scripts"));
        Files.createDirectories(loomDir.resolve("config"));
        Files.createDirectories(loomDir.resolve("libs"));
        Files.createDirectories(loomDir.resolve("plugins"));
    }

    private static void createConfigFile(Path loomDir) throws IOException {
        Path loomToml = loomDir.resolve("loom.toml");
        if (Files.notExists(loomToml)) {
            String defaultConfig = """
                [general]
                hot_reload = true
                reload_debounce_ms = 500
                
                [commands]
                default_prefix = "/"
                tab_complete = true
                default_permission_message = "&cYou don't have permission to use this command."
                disable-vanilla = []
                
                [performance]
                async_variable_writes = true
                event_batch_size = 64
                
                [logging]
                script_errors = "warn"
                """;

            Files.writeString(loomToml, defaultConfig);
            info("Created default config: {}", loomToml.toAbsolutePath());
        } else {
            info("Config file already exists: {}", loomToml.toAbsolutePath());
        }
    }

    private static void createWelcomeScript(Path loomDir) throws IOException {
        Path welcomeScript = loomDir.resolve("scripts").resolve("welcome.ls");
        String script = """
            # scripts/welcome.ls
            
            on player join:
                send "&aWelcome to the server!" to player
            """;
        Files.writeString(welcomeScript, script);
        info("Created example script: {}", welcomeScript.toAbsolutePath());
    }
}