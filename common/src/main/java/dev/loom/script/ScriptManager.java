package dev.loom.script;

import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.HashMap;

import static dev.loom.util.Log.*;

public class ScriptManager {
    private static final Map<String, LoadedScript> scripts = new HashMap<>();
    private static final Map<String, LoadedScript> disabledScripts = new HashMap<>();

    public static int loadAll() {
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
        return scripts.size();
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

    public static boolean reload(String name) {
        Path scriptsDir = Platform.getConfigFolder().resolve("loom").resolve("scripts");
        Path targetFile = scriptsDir.resolve(name + ".ls");

        if (!Files.exists(targetFile)) {
            return false;
        }

        scripts.put(name, new LoadedScript(name, targetFile));

        return true;
    }

    public static boolean disable(String name) {
        LoadedScript script = scripts.get(name);
        if (script == null) {
            return false;
        }

        Path currentPath = script.getPath();
        Path newPath = Path.of(currentPath.toString() + ".disabled");

        try {
            Files.move(currentPath, newPath, StandardCopyOption.ATOMIC_MOVE);
            scripts.remove(name);
            disabledScripts.put(name, new LoadedScript(name, newPath));
        } catch (IOException e) {
            error("Failed to disable script '{}': {}", name, e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean enable(String name) {
        LoadedScript disabledScript = disabledScripts.get(name);
        if (disabledScript == null) {
            return false;
        }

        Path currentPath = disabledScript.getPath();
        String currentPathText = currentPath.toString();
        Path newPath = Path.of(currentPathText.substring(0, currentPathText.length() - 9));

        try {
            Files.move(currentPath, newPath, StandardCopyOption.ATOMIC_MOVE);
            disabledScripts.remove(name);
            scripts.put(name, new LoadedScript(name, newPath));
        } catch (IOException e) {
            error("Failed to enable script '{}': {}", name, e.getMessage());
            return false;
        }

        return true;
    }
}