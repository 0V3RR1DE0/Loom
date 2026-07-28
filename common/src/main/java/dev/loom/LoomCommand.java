package dev.loom;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class LoomCommand {

    private static int help(CommandContext<CommandSourceStack> context) {
        Component message = Component.literal("=== Help Menu ===\n")
                .append(Component.literal("help -- Brings this help menu\n"))
                .append(Component.literal("list -- Lists all scripts\n"))
                .append(Component.literal("reload -- Reloads all scripts, optionally use reload <script name>\n"))
                .append(Component.literal("new <script name> -- Creates a new script file\n"))
                .append(Component.literal("remove <script name> -- Deletes a script\n"))
                .append(Component.literal("rename <script name> <new script name> -- Renames a script\n"))
                .append(Component.literal("enable <script name> -- Enables a script\n"))
                .append(Component.literal("disable <script name> -- Disables a script"));

        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        return 1;
    };

    private static int reload(CommandContext<CommandSourceStack> context) {
        return 1;
    };

    private static int reloadSpecific(CommandContext<CommandSourceStack> context) {
        String scriptName = StringArgumentType.getString(context, "scriptname");
        context.getSource().sendSuccess(() -> Component.literal("Reloading script: " + scriptName), false);
        return 1;
    }


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
                            .then(Commands.argument("scriptname", StringArgumentType.word())
                                    .executes(LoomCommand::reloadSpecific)
                            )
                    );

            dispatcher.register(loomCommand);

            dispatcher.register(Commands.literal("ls")
                    .requires(source -> source.hasPermission(4))
                    .executes(LoomCommand::help)
                    .redirect(dispatcher.getRoot().getChild("loom"))
            );
        });
    }
}