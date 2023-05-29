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

import ch.vorburger.minecraft.storeys.japi.ReadingSpeed;
import ch.vorburger.minecraft.storeys.japi.impl.actions.Narrator;
import ch.vorburger.minecraft.storeys.util.Command;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.inject.Inject;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.world.server.ServerWorld;

public class NarrateCommand implements Command {

    private static final Parameter.Value<String> ARG_ENTITY = Parameter.string().key("storyName").build();
    private static final Parameter.Value<String> ARG_TEXT = Parameter.string().key("text").build();

    private final Narrator narrator;

    @Inject public NarrateCommand(Narrator narrator) {
        this.narrator = narrator;
    }

    @Override public List<String> aliases() {
        return ImmutableList.of("narrate");
    }

    @Override public org.spongepowered.api.command.Command callable() {
        // TODO when Sponge uses entity names instead of UUIDs:
        // TODO requiringPermission()
        return org.spongepowered.api.command.Command.builder().shortDescription(Component.text(("Make an entity character narrate story lines")))
                // .permission("storeys.commands.narrate") ?
                        .addParameter(ARG_ENTITY).addParameter(ARG_TEXT).executor(this).build();

    }

    @Override public CommandResult execute(CommandContext args) throws CommandException {
        String text = args.one(ARG_TEXT).get();

        // TODO when Sponge uses entity names instead of UUIDs:
        // Entity entity = args.<Entity>getOne(ARG_ENTITY).get();
        // narrator.narrate(entity, text, new ReadingSpeed());

        String entityName = args.one(ARG_ENTITY).get();
        final ServerWorld world = args.cause().location().get().world();
        narrator.narrate(world, entityName, text, new ReadingSpeed());

        return CommandResult.success();
    }

}
