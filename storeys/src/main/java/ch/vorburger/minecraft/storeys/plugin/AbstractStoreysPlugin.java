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

import ch.vorburger.minecraft.storeys.ScriptsLoader;
import ch.vorburger.minecraft.storeys.commands.NarrateCommand;
import ch.vorburger.minecraft.storeys.commands.StoryCommand;
import ch.vorburger.minecraft.storeys.guard.GuardGameModeJoinListener;
import ch.vorburger.minecraft.storeys.japi.impl.Scripts;
import ch.vorburger.minecraft.storeys.japi.impl.events.EventService;
import com.google.inject.Injector;
import java.nio.file.Path;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;

// Do *NOT* annotate this class with @Plugin
public abstract class AbstractStoreysPlugin extends AbstractPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractStoreysPlugin.class);

    @Inject @ConfigDir(sharedRoot = false) private Path configDir;

    @Inject protected Injector pluginInjector;

    private Injector childInjector;

    @Inject private EventManager eventManager;

    @Inject private EventService eventService;

    @Listener public final void onGameStartingServer(StartingEngineEvent event) throws Exception {
        LOG.info("See https://github.com/OASIS-learn-study/minecraft-storeys-maker for how to use /story and /narrate commands");
        start(this, configDir);
    }

    protected void start(PluginInstance plugin, Path configDir) throws Exception {
        eventManager.registerListeners(plugin.getPluginContainer(), new GuardGameModeJoinListener());
        eventService.setPluginContainer(plugin.getPluginContainer());

        // TODO(vorburger) child injector might not actually be required, could possibly just use only pluginInjector?
        childInjector = pluginInjector.createChildInjector(binder -> {
            binder.bind(PluginInstance.class).toInstance(plugin);
            binder.bind(Path.class).toInstance(configDir);
            binder.bind(Scripts.class);
            binder.bind(ScriptsLoader.class);
        });
    }

    @Listener public void register(RegisterCommandEvent<Command.Raw> event) {
        final StoryCommand storyCommand = pluginInjector.getInstance(StoryCommand.class);
        final NarrateCommand narrateCommand = pluginInjector.getInstance(NarrateCommand.class);

        event.register(this.getPluginContainer(), (Command.Raw) narrateCommand.callable(), narrateCommand.aliases().get(0),
                narrateCommand.aliases().toArray(new String[0]));
        event.register(this.getPluginContainer(), (Command.Raw) storyCommand.callable(), storyCommand.aliases().get(0),
                storyCommand.aliases().toArray(new String[0]));
    }
}
