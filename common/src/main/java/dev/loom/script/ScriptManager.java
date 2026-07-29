package dev.loom.script;

import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static dev.loom.util.Log.*;

public class ScriptManager {
    private static final Map<String, LoadedScript> scripts = new HashMap<>();
    private static final Map<String, LoadedScript> disabledScripts = new HashMap<>();

    private static final String SCRIPT_EXT = ".ls";
    private static final String DISABLED_EXT = ".disabled";

    private static Path getScriptsDir() {
        return Platform.getConfigFolder().resolve("loom").resolve("scripts");
    }

    public static int loadAll() {
        scripts.clear();
        disabledScripts.clear();
        Path scriptsDir = getScriptsDir();

        try {
            Files.walk(scriptsDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String key = scriptsDir.relativize(path).toString().replace('\\', '/');

                        if (fileName.endsWith(SCRIPT_EXT)) {
                            String scriptName = fileName.substring(0, fileName.length() - SCRIPT_EXT.length());
                            key = key.substring(0, key.length() - SCRIPT_EXT.length());

                            scripts.put(key, new LoadedScript(scriptName, path));
                        } else if (fileName.endsWith(DISABLED_EXT)) {
                            String scriptName = fileName.substring(0, fileName.length() - SCRIPT_EXT.length() - DISABLED_EXT.length());
                            key = key.substring(0, key.length() - SCRIPT_EXT.length() - DISABLED_EXT.length());

                            disabledScripts.put(key, new LoadedScript(scriptName, path));
                        }
                    });
        } catch (IOException e) {
            error("Failed to load scripts from {}", scriptsDir, e);
        }
        info("Loaded {} enabled script(s), {} disabled script(s).", scripts.size(), disabledScripts.size());
        return scripts.size();
    }

    public static boolean createScript(String name) throws IOException {
        Path scripts = getScriptsDir();
        Path targetFile = scripts.resolve(name + SCRIPT_EXT);

        // Path traversal prevention
        Path target = targetFile.normalize();

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
        Path scriptsDir = getScriptsDir();
        Path targetFile = scriptsDir.resolve(name + SCRIPT_EXT).normalize();

        if (!targetFile.startsWith(scriptsDir) || !Files.exists(targetFile)) {
            return false;
        }

        String fileName = targetFile.getFileName().toString();
        String scriptName = fileName.substring(0, fileName.length() - SCRIPT_EXT.length());

        scripts.put(name, new LoadedScript(scriptName, targetFile));

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
            String fileName = newPath.getFileName().toString();
            String scriptName = fileName.substring(0, fileName.length() - DISABLED_EXT.length());

            disabledScripts.put(name, new LoadedScript(scriptName, newPath));
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
        Path newPath = Path.of(currentPathText.substring(0, currentPathText.length() - DISABLED_EXT.length()));

        try {
            Files.move(currentPath, newPath, StandardCopyOption.ATOMIC_MOVE);
            disabledScripts.remove(name);
            String fileName = newPath.getFileName().toString();
            String scriptName = fileName.substring(0, fileName.length() - SCRIPT_EXT.length());

            scripts.put(name, new LoadedScript(scriptName, newPath));
        } catch (IOException e) {
            error("Failed to enable script '{}': {}", name, e.getMessage());
            return false;
        }

        return true;
    }

    public static List<ScriptEntry> getScripts() {
        List<ScriptEntry> list = new ArrayList<>(scripts.size() + disabledScripts.size());

        scripts.forEach((path, script) ->
                list.add(new ScriptEntry(path, true, script)));

        disabledScripts.forEach((path, script) ->
                list.add(new ScriptEntry(path, false, script)));

        list.sort(Comparator.comparing(ScriptEntry::path));

        return list;
    }

    public static Set<String> getScriptNames() {
        return scripts.keySet();
    }

    public static Set<String> getDisabledScriptNames() {
        return disabledScripts.keySet();
    }

    public record ScriptEntry(String path, boolean enabled, LoadedScript script) {}
}