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

import ch.vorburger.minecraft.storeys.plugin.AbstractStoreysPlugin;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "storeys-web", name = "Vorburger.ch's Storeys with Web API", version = "1.0",
    description = "Makes entities narrate story lines so you can make your own movie in Minecraft",
    url = "https://github.com/vorburger/minecraft-storeys-maker",
    authors = "Michael Vorburger.ch")
public class StoreysWebPlugin extends AbstractStoreysPlugin {
    // do not extend StoreysPlugin, because we exclude that class in shadowJar

    @Inject
    private Game game;

    private VertxStarter vertxStarter;

    @Override @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        super.onGameStartingServer(event);

        int httpPort = 8080; // TODO read from some configuration
        vertxStarter = new VertxStarter();
        try {
            vertxStarter.start(httpPort, new ActionsConsumer(this, game)).get();
        } catch (ExecutionException  | InterruptedException e) {
            throw new IllegalStateException("Vert.x start-up failed", e);
        }
    }

    @Override @Listener
    public void onGameStoppingServer(GameStoppingServerEvent event) {
        super.onGameStoppingServer(event);

        vertxStarter.stop();
    }

    @Listener
    // TODO Other Event registrations should later go up into AbstractStoreysPlugin so that Script can have Event triggers as well, but for now:
    public void onPlayerJoin(Join event) {
        JsonObject message = new JsonObject().put("event", "playerJoined").put("player", event.getTargetEntity().getName());
        vertxStarter.send(message);
    }

}
