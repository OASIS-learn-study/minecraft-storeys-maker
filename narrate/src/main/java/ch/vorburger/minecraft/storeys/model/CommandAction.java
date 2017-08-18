package ch.vorburger.minecraft.storeys.model;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;

public class CommandAction implements SynchronousAction<CommandResult> {

    // TODO It would be good if there was a way to know when a /command was "done" ..
    //   to be able to make this an asynchronous Action returning a CompletionStage - but how?

    private String commandLineWithoutSlash;

    public CommandAction setCommand(String commandLine) {
        this.commandLineWithoutSlash = commandLine.trim();
        if (commandLineWithoutSlash.startsWith("/")) {
            commandLineWithoutSlash = commandLineWithoutSlash.substring(1);
        }
        return this;
    }

    @Override
    public CommandResult executeSynchronously(ActionContext context) throws ActionException {
        return Sponge.getCommandManager().process(context.getCommandSource(), commandLineWithoutSlash);
    }

}
