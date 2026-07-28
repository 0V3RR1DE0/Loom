# Loom

A Skript-inspired scripting mod for Fabric and NeoForge servers, with client-side
scripting support. Write custom commands, events, items, and GUIs without needing
a dozen addons.

**Status: early development. Nothing usable yet.**

## Why

Skript is great, but Paper/Spigot only. Loom brings the same idea to modern
Fabric and NeoForge servers — plus scripting on the client side, which Skript
doesn't do at all.

## Supported Versions

Currently targeting Minecraft 1.21.10. See branches for other version support
as it becomes available.

## Building

Requires JDK 25+.

```
./gradlew build
```

Outputs land in `fabric/build/libs/` and `neoforge/build/libs/`.

## License

GPL-3.0. See [LICENSE](LICENSE).

## Changelog

See [CHANGELOG.md](CHANGELOG.md).