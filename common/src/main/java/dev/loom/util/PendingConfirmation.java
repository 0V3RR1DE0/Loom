package dev.loom.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PendingConfirmation {
    private static final Map<UUID, PendingAction> pending = new HashMap<>();

    public record PendingAction(Runnable action, long timestamp, long expiryMillis, String message) {}

    public static void request(UUID uuid, Runnable action, long expiryMillis, String message) {
        pending.put(uuid, new PendingAction(action, System.currentTimeMillis(), expiryMillis, message));
    }

    public enum ConfirmResult {
        NOT_FOUND,
        EXPIRED
    }

    public record ConfirmOutcome(ConfirmResult result, Runnable action, String message) {}

    public static ConfirmOutcome confirm(UUID uuid) {
        PendingAction entry = pending.get(uuid);
        if (entry == null) {
            return new ConfirmOutcome(ConfirmResult.NOT_FOUND, null, null);
        }
        if (System.currentTimeMillis() - entry.timestamp() > entry.expiryMillis()) {
            pending.remove(uuid);
            return new ConfirmOutcome(ConfirmResult.EXPIRED, null, null);
        }
        pending.remove(uuid);
        return new ConfirmOutcome(null, entry.action(), entry.message());
    }
}