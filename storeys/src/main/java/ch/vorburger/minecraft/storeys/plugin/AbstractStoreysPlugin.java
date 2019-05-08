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
package ch.vorburger.minecraft.storeys.plugin;

import ch.vorburger.minecraft.osgi.api.AbstractPlugin;
import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.commands.NarrateCommand;
import ch.vorburger.minecraft.storeys.commands.StoryCommand;
import ch.vorburger.minecraft.storeys.guard.GuardGameModeJoinListener;
import ch.vorburger.minecraft.storeys.util.Commands;
import java.nio.file.Path;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;

// Do *NOT* annotate this class with @Plugin
public abstract class AbstractStoreysPlugin extends AbstractPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractStoreysPlugin.class);

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private CommandMapping narrateCommandMapping;
    private CommandMapping storyCommandMapping;

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) throws Exception {
        LOG.info("See https://github.com/vorburger/minecraft-storeys-maker for how to use /story and /narrate commands");
        start(this, this.configDir);
    }

    protected void start(PluginInstance plugin, @SuppressWarnings("hiding") Path configDir) throws Exception {
        Sponge.getEventManager().registerListener(plugin, Join.class, new GuardGameModeJoinListener());

        storyCommandMapping = Commands.register(plugin, new StoryCommand(plugin, configDir));
        narrateCommandMapping = Commands.register(plugin, new NarrateCommand(plugin));
    }

    @Listener
    public void onGameStoppingServer(GameStoppingServerEvent event) throws Exception {
        stop();
    }

    protected void stop() throws Exception {
        if (narrateCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(narrateCommandMapping);
        }
        if (storyCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(storyCommandMapping);
        }
    }
}
