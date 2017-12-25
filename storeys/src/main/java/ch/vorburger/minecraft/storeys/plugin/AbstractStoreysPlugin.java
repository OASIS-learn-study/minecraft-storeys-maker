/**
 * ch.vorburger.minecraft.storeys
 *
 * Copyright (C) 2016 - 2017 Michael Vorburger.ch <mike@vorburger.ch>
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
package ch.vorburger.minecraft.storeys.plugin;

import ch.vorburger.minecraft.osgi.api.AbstractPlugin;
import ch.vorburger.minecraft.storeys.commands.NarrateCommand;
import ch.vorburger.minecraft.storeys.commands.StoryCommand;
import ch.vorburger.minecraft.storeys.util.Commands;
import java.nio.file.Path;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;

// Do *NOT* annotate this class with @Plugin
public abstract class AbstractStoreysPlugin extends AbstractPlugin {

    @Inject
    protected Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private CommandMapping narrateCommandMapping;
    private CommandMapping storyCommandMapping;

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        logger.info("See https://github.com/vorburger/minecraft-storeys-maker for how to use /story and /narrate commands");
        storyCommandMapping = Commands.register(this, new StoryCommand(this, configDir));
        narrateCommandMapping = Commands.register(this, new NarrateCommand(this));
    }

    @Listener
    public void onGameStoppingServer(GameStoppingServerEvent event) {
        if (narrateCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(narrateCommandMapping);
        }
        if (storyCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(storyCommandMapping);
        }
    }

}
