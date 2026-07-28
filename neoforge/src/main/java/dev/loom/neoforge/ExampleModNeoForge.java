package dev.loom.neoforge;

import net.neoforged.fml.common.Mod;

import dev.loom.LoomCore;

@Mod(LoomCore.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        LoomCore.init();
    }
}
