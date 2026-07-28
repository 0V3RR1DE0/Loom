package dev.loom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    private static final Logger LOGGER = LoggerFactory.getLogger("Loom");

    // Direct pass-through to SLF4J (supports {} placeholders)
    public static void info(String msg, Object... args) {
        LOGGER.info(msg, args);
    }

    public static void error(String msg, Object... args) {
        LOGGER.error(msg, args);
    }

    public static void error(String msg, Throwable t, Object... args) {
        LOGGER.error(msg, t, args);
    }

    public static void warn(String msg, Object... args) {
        LOGGER.warn(msg, args);
    }

    public static void debug(String msg, Object... args) {
        LOGGER.debug(msg, args);
    }

    public static void trace(String msg, Object... args) {
        LOGGER.trace(msg, args);
    }

    // With prefix
    public static void info(String prefix, String msg, Object... args) {
        LOGGER.info("[{}] " + msg, prefix, args);
    }

    public static void error(String prefix, String msg, Object... args) {
        LOGGER.error("[{}] " + msg, prefix, args);
    }

    public static void error(String prefix, String msg, Throwable t, Object... args) {
        LOGGER.error("[{}] " + msg, prefix, t, args);
    }

    public static void warn(String prefix, String msg, Object... args) {
        LOGGER.warn("[{}] " + msg, prefix, args);
    }

    public static void debug(String prefix, String msg, Object... args) {
        LOGGER.debug("[{}] " + msg, prefix, args);
    }

    public static void trace(String prefix, String msg, Object... args) {
        LOGGER.trace("[{}] " + msg, prefix, args);
    }
}