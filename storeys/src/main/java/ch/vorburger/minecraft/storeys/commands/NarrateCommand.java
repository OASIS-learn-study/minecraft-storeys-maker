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
package ch.vorburger.minecraft.storeys.commands;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.remainingJoinedStrings;
import static org.spongepowered.api.text.Text.of;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import ch.vorburger.minecraft.storeys.util.Command;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.extent.EntityUniverse;

public class NarrateCommand implements Command {

    private static final Text ARG_ENTITY = of("entity");
    private static final Text ARG_TEXT = of("text");

    private final Narrator narrator;

    public NarrateCommand(PluginInstance plugin) {
        narrator = new Narrator(plugin);
    }

    @Override
    public List<String> aliases() {
        return ImmutableList.of("narrate");
    }

    @Override
    public CommandCallable callable() {
        return CommandSpec.builder()
            .description(Text.of("Make an entity character narrate story lines"))
         // .permission("storeys.commands.narrate") ?
            .arguments(
                    // TODO when Sponge uses entity names instead of UUIDs:
                    // onlyOne(entity(ARG_ENTITY)), // TODO requiringPermission()
                    onlyOne(GenericArguments.string(ARG_ENTITY)), // TODO requiringPermission()
                    remainingJoinedStrings(ARG_TEXT) // remainingRawJoinedStrings ?
            ).executor(this).build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String text = args.<String>getOne(ARG_TEXT).get();

        // TODO when Sponge uses entity names instead of UUIDs:
        // Entity entity = args.<Entity>getOne(ARG_ENTITY).get();
        // narrator.narrate(entity, text, new ReadingSpeed());

        String entityName = args.<String>getOne(ARG_ENTITY).get();
        EntityUniverse world = ((Locatable) src).getWorld();
        narrator.narrate(world , entityName, text, new ReadingSpeed());

        return CommandResult.success();
    }
}
