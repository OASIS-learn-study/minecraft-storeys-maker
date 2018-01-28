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
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
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

    private static final Logger LOG = LoggerFactory.getLogger(StoreysWebPlugin.class);

    // no @Inject but ugly static, so that this works from the Activator as well
    private final Game game = Sponge.getGame();

    private VertxStarter vertxStarter;
    private EventService eventService;
    private ActionsConsumer actionsConsumer;

    @Override
    public void start(PluginInstance plugin, Path configDir) {
        super.start(plugin, configDir);
        try {
            int httpPort = 8080; // TODO read from some configuration
            vertxStarter = new VertxStarter();
            eventService = new EventService(plugin);
            try {
                actionsConsumer = new ActionsConsumer(plugin, game, eventService, new ConditionService(plugin), vertxStarter);
                vertxStarter.start(httpPort, actionsConsumer).get();
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
    public void stop() {
        actionsConsumer.stop();
        vertxStarter.stop();
        super.stop();
    }

    // TODO Other Event registrations should later go up into AbstractStoreysPlugin so that Script can have Event triggers as well, but for now:

    @Listener
    public void onPlayerJoin(Join event) throws Exception {
        eventService.onPlayerJoin(event);
    }

    @Listener
    public void onInteractEntityEvent(InteractEntityEvent event) {
        eventService.onInteractEntityEvent(event);
    }

    @Listener
    public void onChangeInventoryEvent(ChangeInventoryEvent.Held event) {
        LOG.info("onChangeInventory event={}", event);
        LOG.info("onChangeInventory event={}", event.getTargetInventory().first().toString());

        Optional<Player> optPlayer = event.getCause().first(Player.class);
        optPlayer.ifPresent(player -> {
            LOG.info("onChangeInventory item.id={}", player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getId());
            LOG.info("onChangeInventory item.name={}", player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getName());
            LOG.info("onChangeInventory item.type.id={}", player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getType().getId());
            LOG.info("onChangeInventory item.type.name={}", player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getType().getName());
        });
    }

//  @Listener
//  public void onInteractItemEvent(InteractItemEvent event) {
//      logger.info("InteractItemEvent: {}", event);
//  }

}
