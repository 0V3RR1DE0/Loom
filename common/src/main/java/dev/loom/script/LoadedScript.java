package dev.loom.script;

import java.nio.file.Path;

public class LoadedScript {
    private final String name;
    private final Path path;

    public LoadedScript(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }
}
