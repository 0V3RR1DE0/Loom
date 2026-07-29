package dev.loom.script;

import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;

import static dev.loom.util.Log.*;

public class ScriptManager {
    private static final Map<String, LoadedScript> scripts = new HashMap<>();

    public static void loadAll() {
        scripts.clear();
        Path scriptsDir = Platform.getConfigFolder().resolve("loom").resolve("scripts");

        try {
            Files.walk(scriptsDir)
                    .filter(path -> path.toString().endsWith(".ls"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String scriptName = fileName.substring(0, fileName.length() - 3); // strip ".ls"
                        scripts.put(scriptName, new LoadedScript(scriptName, path));
                    });
        } catch (IOException e) {
            error("Failed to load scripts from {}", scriptsDir, e);
        }
        info("Loaded {} script(s).", scripts.size());
    }

    public static boolean createScript(String name) throws IOException {
        Path targetFile = Platform.getConfigFolder().resolve("loom").resolve("scripts").resolve(name + ".ls");

        // Path traversal prevention
        Path scripts = Platform.getConfigFolder().resolve("loom").resolve("scripts");
        Path target = scripts.resolve(name + ".ls").normalize();

        if (!target.startsWith(scripts)) {
            throw new IllegalArgumentException("Invalid script path.");
        }

        if (Files.exists(targetFile)) {
            return false;
        }

        Files.createDirectories(targetFile.getParent());
        Files.createFile(targetFile);

        return true;
    }
}