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
import ch.vorburger.minecraft.storeys.ScriptsLoader;
import ch.vorburger.minecraft.storeys.commands.NarrateCommand;
import ch.vorburger.minecraft.storeys.commands.StoryCommand;
import ch.vorburger.minecraft.storeys.guard.GuardGameModeJoinListener;
import ch.vorburger.minecraft.storeys.japi.impl.Scripts;
import ch.vorburger.minecraft.storeys.japi.impl.Unregisterable;
import ch.vorburger.minecraft.storeys.japi.impl.events.EventService;
import ch.vorburger.minecraft.storeys.util.Commands;
import com.google.inject.Injector;
import java.nio.file.Path;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;

// Do *NOT* annotate this class with @Plugin
public abstract class AbstractStoreysPlugin extends AbstractPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractStoreysPlugin.class);

    @Inject
    @ConfigDir(sharedRoot = false) private Path configDir;

    @Inject protected Injector pluginInjector;
    private Injector childInjector;

    @Inject private EventManager eventManager;

    @Inject private EventService eventService;

    @Inject private CommandManager commandManager;

    private CommandMapping narrateCommandMapping;
    private CommandMapping storyCommandMapping;

    @Listener public final void onGameStartingServer(GameStartingServerEvent event) throws Exception {
        LOG.info("See https://github.com/OASIS-learn-study/minecraft-storeys-maker for how to use /story and /narrate commands");
        start(this, configDir);
    }

    protected void start(PluginInstance plugin, Path configDir) throws Exception {
        eventManager.registerListener(plugin, Join.class, new GuardGameModeJoinListener());
        eventService.setPluginInstance(plugin);

        // TODO(vorburger) child injector might not actually be required, could possibly just use only pluginInjector?
        childInjector = pluginInjector.createChildInjector(binder -> {
            binder.bind(PluginInstance.class).toInstance(plugin);
            binder.bind(Path.class).toInstance(configDir);
            binder.bind(Scripts.class);
            binder.bind(ScriptsLoader.class);
        });
        storyCommandMapping = Commands.register(plugin, pluginInjector.getInstance(StoryCommand.class));
        narrateCommandMapping = Commands.register(plugin, pluginInjector.getInstance(NarrateCommand.class));
    }

    @Listener public final void onGameStoppingServer(GameStoppingServerEvent event) throws Exception {
        stop();
    }

    protected void stop() throws Exception {
        childInjector.getInstance(Scripts.class).getUnregisterables().forEach(Unregisterable::unregister);
        childInjector.getInstance(ScriptsLoader.class).close();

        if (narrateCommandMapping != null) {
            commandManager.removeMapping(narrateCommandMapping);
        }
        if (storyCommandMapping != null) {
            commandManager.removeMapping(storyCommandMapping);
        }
    }
}
