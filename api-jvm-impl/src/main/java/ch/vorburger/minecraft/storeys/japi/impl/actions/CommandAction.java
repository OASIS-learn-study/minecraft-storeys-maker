/*
 * ch.vorburger.minecraft.storeys
 *
 * Copyright (C) 2016 - 2018 Michael Vorburger.ch <mike@vorburger.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.vorburger.minecraft.storeys.japi.impl.actions;

import static java.util.Objects.requireNonNull;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.japi.ActionContext;
import java.util.StringJoiner;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.scheduler.Scheduler;

public class CommandAction extends MainThreadAction<CommandResult> {

    private static final Logger LOG = LoggerFactory.getLogger(CommandAction.class);

    // TODO It would be good if there was a way to know when a /command was "done" ..
    // to be able to make this an asynchronous Action returning a CompletionStage - but how?

    private String commandLineWithoutSlash;

    @Inject public CommandAction(PluginInstance plugin, Scheduler scheduler) {
        super(plugin, scheduler);
    }

    public CommandAction setCommand(String commandLine) {
        this.commandLineWithoutSlash = requireNonNull(commandLine, "commandLine").trim();
        if (commandLineWithoutSlash.startsWith("/")) {
            commandLineWithoutSlash = commandLineWithoutSlash.substring(1);
        }
        return this;
    }

    @Override public void setParameter(String param) {
        setCommand(param);
    }

    @Override protected CommandResult executeInMainThread(ActionContext context) throws ActionException {
        CommandResult result = Sponge.getCommandManager().process(context.getCommandCause(),
                requireNonNull(commandLineWithoutSlash, "commandLineWithoutSlash"));
        LOG.info("processed command \"/{}\" from source {} with result {}", commandLineWithoutSlash, context.getCommandCause(),
                toString(result));
        return result;
    }

    private String toString(CommandResult result) {
        StringJoiner sj = new StringJoiner(", ", "{", "}");
        result.getAffectedBlocks().ifPresent(affectedBlocked -> sj.add("affectedBlocked: " + affectedBlocked));
        result.getAffectedEntities().ifPresent(affectedEntities -> sj.add("affectedEntities: " + affectedEntities));
        result.getAffectedItems().ifPresent(affectedItems -> sj.add("affectedItems: " + affectedItems));
        result.getQueryResult().ifPresent(queryResult -> sj.add("queryResult: " + queryResult));
        result.getSuccessCount().ifPresent(successCount -> sj.add("successCount: " + successCount));
        return sj.toString();
    }

    @Override public String toString() {
        return getClass().getSimpleName() + ": " + (commandLineWithoutSlash != null ? ("/" + commandLineWithoutSlash) : "null");
    }
}
