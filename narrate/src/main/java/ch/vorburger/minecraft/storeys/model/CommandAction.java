package ch.vorburger.minecraft.storeys.model;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;

public class CommandAction implements SynchronousAction<CommandResult> {

    private String commandLineWithoutSlash;

    public CommandAction setCommand(String commandLine) {
        this.commandLineWithoutSlash = commandLine.trim();
        if (commandLineWithoutSlash.startsWith("/")) {
            commandLineWithoutSlash = commandLineWithoutSlash.substring(1);
        }
        return this;
    }

    @Override
    public CommandResult executeSynchronously(CommandSource src) throws ActionException {
        return Sponge.getCommandManager().process(src, commandLineWithoutSlash);
    }

}
