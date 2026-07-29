package dev.loom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.loom.script.ScriptManager;
import dev.loom.util.ScriptSuggestions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static dev.loom.util.Log.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class LoomCommand {

    private static final int SCRIPTS_PER_PAGE = 12;

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {

            var loomCommand = Commands.literal("loom")
                    .requires(source -> source.hasPermission(4))
                    .executes(LoomCommand::help)
                    .then(Commands.literal("help")
                            .executes(LoomCommand::help)
                    )
                    .then(Commands.literal("list")
                            .executes(ctx -> list(ctx, 1, null))
                            .then(Commands.literal("enabled")
                                    .executes(ctx -> list(ctx, 1, true))
                                    .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                            .executes(ctx -> list(ctx, IntegerArgumentType.getInteger(ctx, "page"), true))
                                    )
                            )
                            .then(Commands.literal("disabled")
                                    .executes(ctx -> list(ctx, 1, false))
                                    .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                            .executes(ctx -> list(ctx, IntegerArgumentType.getInteger(ctx, "page"), false))
                                    )
                            )
                            .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                    .executes(ctx -> list(ctx, IntegerArgumentType.getInteger(ctx, "page"), null))
                            )
                    )
                    .then(Commands.literal("reload")
                            .executes(LoomCommand::reload)
                            .then(Commands.argument("scriptname", StringArgumentType.greedyString())
                                    .suggests(ScriptSuggestions.ENABLED)
                                    .executes(LoomCommand::reloadSpecific)
                            )
                    )
                    .then(Commands.literal("enable")
                            .then(Commands.argument("scriptname", StringArgumentType.greedyString())
                                    .suggests(ScriptSuggestions.DISABLED)
                                    .executes(LoomCommand::enableSpecific)
                            )
                    )
                    .then(Commands.literal("disable")
                            .then(Commands.argument("scriptname", StringArgumentType.greedyString())
                                    .suggests(ScriptSuggestions.ENABLED)
                                    .executes(LoomCommand::disableSpecific)
                            )
                    )
                    .then(Commands.literal("new")
                            .then(Commands.argument("scriptname", StringArgumentType.greedyString())
                                    .executes(LoomCommand::createScript)
                            )
                    )
                    .then(Commands.literal("remove")
                            .then(Commands.argument("scriptname", StringArgumentType.greedyString())
                                    .suggests(ScriptSuggestions.ALL)
                                    .executes(LoomCommand::remove)
                            )
                    )
                    .then(Commands.literal("rename")
                            .then(Commands.argument("scriptname", StringArgumentType.greedyString())
                                    .suggests(ScriptSuggestions.ALL)
                                    .then(Commands.argument("newname", StringArgumentType.greedyString())
                                            .executes(LoomCommand::rename)
                                    )
                            )
                    )
                    .then(Commands.literal("confirm")
                            .executes(LoomCommand::confirm)
                    );

            dispatcher.register(loomCommand);

            dispatcher.register(Commands.literal("ls")
                    .requires(source -> source.hasPermission(4))
                    .executes(LoomCommand::help)
                    .redirect(dispatcher.getRoot().getChild("loom"))
            );
        });
    }

    private static int enableSpecific(CommandContext<CommandSourceStack> context) {
        String scriptName = StringArgumentType.getString(context, "scriptname");

        boolean success = ScriptManager.enable(scriptName);
        if (success) {
            context.getSource().sendSuccess(() -> Component.literal("Enabled script: " + scriptName), false);
        } else {
            context.getSource().sendFailure(Component.literal("No script named '" + scriptName + "' found."));
        }

        return 1;
    }

    private static int disableSpecific(CommandContext<CommandSourceStack> context) {
        String scriptName = StringArgumentType.getString(context, "scriptname");

        boolean success = ScriptManager.disable(scriptName);
        if (success) {
            context.getSource().sendSuccess(() -> Component.literal("Disabled script: " + scriptName), false);
        } else {
            context.getSource().sendFailure(Component.literal("No script named '" + scriptName + "' found."));
        }

        return 1;
    }

    private static int rename(CommandContext<CommandSourceStack> context) {
        return 1;
    }

    private static int confirm(CommandContext<CommandSourceStack> context) {
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> context) {
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context, int page, Boolean enabledFilter) {
        List<ScriptManager.ScriptEntry> scripts = ScriptManager.getScripts();

        if (enabledFilter != null) {
            scripts.removeIf(entry -> entry.enabled() != enabledFilter);
        }

        int totalScripts = scripts.size();
        int totalPages = Math.max(1, (totalScripts + SCRIPTS_PER_PAGE - 1) / SCRIPTS_PER_PAGE);

        final int currentPage = Math.max(1, Math.min(page, totalPages));

        int start = (currentPage - 1) * SCRIPTS_PER_PAGE;
        int end = Math.min(start + SCRIPTS_PER_PAGE, totalScripts);

        context.getSource().sendSuccess(() -> Component.literal("=== Loom Scripts List ===").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false );

        for (int i = start; i < end; i++) {
            ScriptManager.ScriptEntry entry = scripts.get(i);
            MutableComponent line = Component.literal("● " + entry.path()).withStyle(entry.enabled() ? ChatFormatting.WHITE : ChatFormatting.GRAY);

            context.getSource().sendSuccess(() -> line, false);
        }
        context.getSource().sendSuccess(() -> Component.literal("◀ Previous ").withStyle(ChatFormatting.DARK_GRAY)
                                .append(Component.literal("Page " + currentPage + "/" + totalPages)
                                        .withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(" Next ▶")
                                        .withStyle(ChatFormatting.DARK_GRAY)), false);
        return 1;
    }

    private static int createScript(CommandContext<CommandSourceStack> context) {
        String scriptName = StringArgumentType.getString(context, "scriptname");

        if (scriptName.contains(" ")) {
            context.getSource().sendFailure(Component.literal("Script names cannot contain spaces."));
            return 1;
        }

        try {
            boolean success = ScriptManager.createScript(scriptName);
            if (success) {
                Path scriptPath = Path.of(scriptName);

                String fileName = scriptPath.getFileName().toString();
                Path parent = scriptPath.getParent();

                if (parent == null) {
                    context.getSource().sendSuccess(() -> Component.literal("Script '" + fileName + "' created."), false);
                } else {
                    context.getSource().sendSuccess(
                            () -> Component.literal("Script '" + fileName + "' created in '" + parent + "'."), false);
                }
            } else {
                context.getSource().sendFailure(
                        Component.literal("A script named '" + scriptName + "' already exists.")
                );
            }
        } catch (IOException | IllegalArgumentException e) {
            error("Failed to create script '{}': {}", scriptName, e.getMessage());
            context.getSource().sendFailure(Component.literal("Failed to create script '" + scriptName + "': " + e.getMessage())
        );
    }
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("=== Loom Commands ===")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);

        String[][] entries = {
                {"help", "Shows this help menu"},
                {"list", "Lists all loaded scripts"},
                {"reload", "Reloads all scripts"},
                {"reload <name>", "Reloads a single script"},
                {"new <name>", "Creates a new script file"},
                {"remove <name>", "Deletes a script"},
                {"rename <name> <new name>", "Renames a script"},
                {"enable <name>", "Enables a disabled script"},
                {"disable <name>", "Disables an active script"},
                {"confirm", "Confirms a pending destructive action"}
        };

        for (String[] entry : entries) {
            String usage = entry[0];
            String description = entry[1];

            MutableComponent line = Component.literal("/loom " + usage)
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" - ")
                            .withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(description)
                            .withStyle(ChatFormatting.WHITE));

            context.getSource().sendSuccess(() -> line, false);
        }

        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        int count = ScriptManager.loadAll();
        context.getSource().sendSuccess(() -> Component.literal("Reloaded " + count + " script(s)."), false);
        return 1;
    };

    private static int reloadSpecific(CommandContext<CommandSourceStack> context) {
        String scriptName = StringArgumentType.getString(context, "scriptname");

        if (scriptName.contains(" ")) {
            context.getSource().sendFailure(Component.literal("Script names cannot contain spaces."));
            return 1;
        }

        boolean success = ScriptManager.reload(scriptName);
        if (success) {
            context.getSource().sendSuccess(() -> Component.literal("Reloaded script: " + scriptName), false);
        } else {
            context.getSource().sendFailure(Component.literal("No script named '" + scriptName + "' found."));
        }

        return 1;
    }
}