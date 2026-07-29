package dev.loom.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.loom.script.ScriptManager;
import net.minecraft.commands.CommandSourceStack;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ScriptSuggestions {
    private ScriptSuggestions() {}

    public static final SuggestionProvider<CommandSourceStack> ENABLED =
            (context, builder) -> {
                suggest(builder, ScriptManager.getScriptNames());
                return builder.buildFuture();
            };

    public static final SuggestionProvider<CommandSourceStack> DISABLED =
            (context, builder) -> {
                suggest(builder, ScriptManager.getDisabledScriptNames());
                return builder.buildFuture();
            };

    public static final SuggestionProvider<CommandSourceStack> ALL =
            (context, builder) -> {
                suggest(builder, ScriptManager.getScriptNames());
                suggest(builder, ScriptManager.getDisabledScriptNames());
                return builder.buildFuture();
            };

    private static void suggest(SuggestionsBuilder builder, Set<String> scripts) {
        String remaining = builder.getRemaining().replace('\\', '/');

        for (String script : scripts) {
            if (script.regionMatches(true, 0, remaining, 0, remaining.length())) {
                builder.suggest(script);
            }
        }
    }
}