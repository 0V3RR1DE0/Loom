package dev.loom.neoforge;

import net.neoforged.fml.common.Mod;

import dev.loom.LoomCore;

@Mod(LoomCore.MOD_ID)
public final class LoomNeoForge {
    public LoomNeoForge() {
        // Run our common setup.
        LoomCore.init();
    }
}
