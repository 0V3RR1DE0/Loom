package dev.loom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.loom.script.ScriptManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import static dev.loom.util.Log.*;

import java.io.IOException;
import java.nio.file.Path;

public class LoomCommand {

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {

            var loomCommand = Commands.literal("loom")
                    .requires(source -> source.hasPermission(4))
                    .executes(LoomCommand::help)
                    .then(Commands.literal("help")
                            .executes(LoomCommand::help)
                    )
                    .then(Commands.literal("list")
                            .executes(LoomCommand::list)
                    )
                    .then(Commands.literal("reload")
                            .executes(LoomCommand::reload)
                            .then(Commands.argument("scriptname", StringArgumentType.greedyString())
                                    .executes(LoomCommand::reloadSpecific)
                            )
                    )
                    .then(Commands.literal("enable")
                            .then(Commands.argument("scriptname", StringArgumentType.greedyString())
                                    .executes(LoomCommand::enableSpecific)
                            )
                    )
                    .then(Commands.literal("disable")
                            .then(Commands.argument("scriptname", StringArgumentType.greedyString())
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
                                    .executes(LoomCommand::remove)
                            )
                    )
                    .then(Commands.literal("rename")
                            .then(Commands.argument("scriptname", StringArgumentType.greedyString())
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
        Component message = Component.literal("=== Help Menu ===\n")
                .append(Component.literal("help -- Brings this help menu\n"))
                .append(Component.literal("list -- Lists all scripts\n"))
                .append(Component.literal("reload -- Reloads all scripts, optionally use reload <script name>\n"))
                .append(Component.literal("new <script name> -- Creates a new script file\n"))
                .append(Component.literal("remove <script name> -- Deletes a script\n"))
                .append(Component.literal("rename <script name> <new script name> -- Renames a script\n"))
                .append(Component.literal("enable <script name> -- Enables a script\n"))
                .append(Component.literal("disable <script name> -- Disables a script\n"))
                .append(Component.literal("confirm -- Confirms an action"));

        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        return 1;
    };

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