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
package ch.vorburger.minecraft.storeys.events;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.util.Command;
import ch.vorburger.minecraft.storeys.util.Commands;
import ch.vorburger.minecraft.utils.CommandExceptions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;

public class ScriptCommand implements Command, Unregisterable {

    public ScriptCommand(String commandName, PluginInstance plugin, Callback callback) {
        this.aliases = ImmutableList.of(commandName);
        this.callback = callback;
        this.commandMapping = Commands.register(plugin, this);
    }

    private final Callback callback;

    private final CommandMapping commandMapping;

    private final ImmutableList<String> aliases;

    @Override
    public List<String> aliases() {
        return aliases;
    }

    @Override
    public CommandCallable callable() {
        return CommandSpec.builder().executor(this).build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CommandExceptions.doOrThrow("Failed, due to: ", () -> callback.call((Player) src));
        return CommandResult.success();
    }

    @Override
    public void unregister() {
        Sponge.getCommandManager().removeMapping(commandMapping);
    }

}
