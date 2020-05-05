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
package ch.vorburger.minecraft.storeys.web;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.events.EventService;
import ch.vorburger.minecraft.storeys.events.ScriptCommand;
import ch.vorburger.minecraft.storeys.events.Unregisterable;
import ch.vorburger.minecraft.storeys.simple.impl.NotLoggedInException;
import com.google.common.base.Splitter;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.Objects.requireNonNull;

/**
 * Vert.x EventBus consumer handler.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class ActionsConsumer implements Handler<Message<JsonObject>> {

    private static final Logger LOG = LoggerFactory.getLogger(ActionsConsumer.class);
    private static final Splitter SLASH_SPLITTER = Splitter.on('/');

    // TODO Most of these should completely move into MinecraftImpl...
    private final PluginInstance plugin;
    private final EventService eventService;
    private final EventBusSender eventBusSender;

    private final Map<String, Unregisterable> conditionRegistrations = new ConcurrentHashMap<>();
    private final Map<String, Pair<Location<World>, Location<World>>> playerBoxLocations = new ConcurrentHashMap<>();

    @Inject
    public ActionsConsumer(PluginInstance plugin, EventService eventService, EventBusSender eventBusSender) {
        this.plugin = plugin;
        this.eventBusSender = eventBusSender;

        this.eventService = eventService;

        eventService.registerPlayerJoin(event -> {
            Player player = event.getTargetEntity();
            JsonObject message = new JsonObject().put("event", "playerJoined").put("player", player.getName())
                    .put("playerUUID", player.getUniqueId().toString());
            eventBusSender.send(message);
        });
    }

    @Override
    public void handle(Message<JsonObject> message) {
        LOG.info("Handling (old style) action message received on EventBus: {}", message.body().encodePrettily());

        JsonObject json = message.body();
        String playerUUID = json.getString("playerUUID");

        try {
            switch (json.getString("action")) {
            case "ping": {
                message.reply("pong");
                LOG.info("ping & pong ACK reply");
                break;
            }
            case "registerCondition": {
                String condition = json.getString("condition");
                registerCondition(requireNonNull(condition, "condition"));
                message.reply(condition);
                break;
            }
            default:
                LOG.error("Unknown action in message: " + message.body().encodePrettily());
                break;
            }
        } catch (Exception e) {
            // TODO make red etc. like in that command helper
            LOG.error("caught Exception", e);
            Player player = getPlayer(playerUUID);
            player.sendMessage(Text.of(e.getMessage()));
            throw e;
        }
    }

    public void stop() {
        for (Unregisterable reg : conditionRegistrations.values()) {
            reg.unregister();
        }
    }

    private void registerCondition(String conditionAsText) {
        if (runIfStartsWith(conditionAsText, "newCmd", commandName -> {
            ScriptCommand scriptCommand = new ScriptCommand(commandName, plugin, (Player player) -> {
                eventBusSender.send(new JsonObject().put("event", conditionAsText).put("playerUUID", player.getUniqueId().toString()));
            });
            conditionRegistrations.put(conditionAsText, scriptCommand);
        })) {} else if (runIfStartsWith(conditionAsText, "entity_interaction:", entityNameSlashInteraction -> {
            Iterator<String> parts = SLASH_SPLITTER.split(entityNameSlashInteraction).iterator();
            String entityName = parts.next();
            // String interaction = parts.next();
            conditionRegistrations.put(conditionAsText, eventService.registerInteractEntity(entityName, (Player player) -> {
                eventBusSender.send(new JsonObject().put("event", conditionAsText).put("playerUUID", player.getUniqueId().toString()));
            }));
        })) {} else if (runIfStartsWith(conditionAsText, "playerJoined", empty -> {
            // Ignore (we registered for this globally, above)
        })) {} else if (runIfStartsWith(conditionAsText, "player_inside", empty -> {
            // Ignore (we registered for this globally, above)
        })) {} else {
            LOG.error("Unknown condition: " + conditionAsText);
        }
    }

    private Player getPlayer(String playerUUID) {
        requireNonNull(playerUUID, "playerUUID");
        return Sponge.getGame().getServer().getPlayer(UUID.fromString(playerUUID)).orElseThrow(() -> new NotLoggedInException(playerUUID));
    }

    private boolean runIfStartsWith(String conditionAsText, String prefix, Consumer<String> consumer) {
        if (conditionAsText.startsWith(prefix)) {
            String args = conditionAsText.substring(prefix.length());
            consumer.accept(args);
            return true;
        } else {
            return false;
        }
    }
}
