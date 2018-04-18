/**
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
package ch.vorburger.minecraft.storeys.web;

import ch.vorburger.minecraft.osgi.api.Listeners;
import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.events.EventService;
import ch.vorburger.minecraft.storeys.plugin.AbstractStoreysPlugin;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.simple.impl.MinecraftImpl;
import ch.vorburger.minecraft.storeys.simple.impl.TokenProviderImpl;
import ch.vorburger.minecraft.storeys.util.Commands;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "storeys-web", name = "Vorburger.ch's Storeys with Web API", version = "1.0",
    description = "Makes entities narrate story lines so you can make your own movie in Minecraft",
    url = "https://github.com/vorburger/minecraft-storeys-maker",
    authors = "Michael Vorburger.ch")
public class StoreysWebPlugin extends AbstractStoreysPlugin implements Listeners {
    // do not extend StoreysPlugin, because we exclude that class in shadowJar

    // no @Inject but ugly static, so that this works from the Activator as well
    private final Game game = Sponge.getGame();

    private VertxStarter vertxStarter;
    private EventService eventService;
    private ActionsConsumer actionsConsumer;

    private CommandMapping loginCommandMapping;

    @Override
    public void start(PluginInstance plugin, Path configDir) throws Exception {
        super.start(plugin, configDir);

        // TODO Other Event registrations should later go up into AbstractStoreysPlugin so that Script can have Event triggers as well, but for now:
        EventManager eventManager = Sponge.getEventManager();
        eventManager.registerListener(plugin, Join.class, event -> eventService.onPlayerJoin(event));
        eventManager.registerListener(plugin, InteractEntityEvent.class, event -> eventService.onInteractEntityEvent(event));
        eventManager.registerListener(plugin, ChangeInventoryEvent.Held.class, event -> eventService.onChangeInventoryHeldEvent(event));
        // InteractItemEvent ?

        TokenProvider tokenProvider = new TokenProviderImpl(game);
        MinecraftImpl minecraft = new MinecraftImpl(plugin, tokenProvider);
        loginCommandMapping = Commands.register(plugin, new LoginCommand(tokenProvider));

        try {
            int httpPort = 8080; // TODO read from some configuration
            vertxStarter = new VertxStarter();
            eventService = new EventService(plugin);
            try {
                actionsConsumer = new ActionsConsumer(plugin, eventService, new ConditionService(plugin), vertxStarter, tokenProvider, minecraft);
                vertxStarter.start(httpPort, actionsConsumer).toCompletableFuture().get();
                vertxStarter.deployVerticle(new StaticWebServerVerticle(7070)).toCompletableFuture().get();
            } catch (ExecutionException  | InterruptedException e) {
                throw new IllegalStateException("Vert.x start-up failed", e);
            }
        } catch (RuntimeException e) {
            // If something went wrong during the Vert.x set up, we must unregister the commands registered in super.start()
            // so that, under OSGi, we'll manage to cleanly restart when whatever problem caused the start up to fail is fixed again.
            super.stop();
            throw e;
        }
    }

    @Override
    public void stop() throws Exception {
        if (loginCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(loginCommandMapping);
        }
        if (actionsConsumer != null) {
            actionsConsumer.stop();
        }
        if (vertxStarter != null) {
            vertxStarter.stop();
        }
        super.stop();
    }

}
