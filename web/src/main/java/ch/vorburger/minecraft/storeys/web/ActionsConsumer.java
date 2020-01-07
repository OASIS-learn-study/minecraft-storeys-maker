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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.events.Condition;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.events.ConditionService.ConditionServiceRegistration;
import ch.vorburger.minecraft.storeys.events.EventService;
import ch.vorburger.minecraft.storeys.events.LocatableInBoxCondition;
import ch.vorburger.minecraft.storeys.events.ScriptCommand;
import ch.vorburger.minecraft.storeys.events.Unregisterable;
import ch.vorburger.minecraft.storeys.model.LocationToolAction;
import ch.vorburger.minecraft.storeys.simple.impl.NotLoggedInException;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Splitter;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

/**
 * Vert.x EventBus consumer handler.
 *
 * @author Michael Vorburger.ch
 */
public class ActionsConsumer implements Handler<Message<JsonObject>> {

    private static final Logger LOG = LoggerFactory.getLogger(ActionsConsumer.class);
    private static final Splitter SLASH_SPLITTER = Splitter.on('/');

    // TODO Most of these should completely move into MinecraftImpl...
    private final PluginInstance plugin;
    private final EventService eventService;
    private final ConditionService conditionService;
    private final EventBusSender eventBusSender;

    private final Map<String, Unregisterable> conditionRegistrations = new ConcurrentHashMap<>();
    private final Map<String, Pair<Location<World>, Location<World>>> playerBoxLocations = new ConcurrentHashMap<>();

    @Inject
    public ActionsConsumer(PluginInstance plugin, EventService eventService,
            ConditionService conditionService, EventBusSender eventBusSender) {
        this.plugin = plugin;
        this.eventBusSender = eventBusSender;

        this.eventService = eventService;
        this.conditionService = conditionService;

        eventService.registerPlayerJoin(event -> {
            Player player = event.getTargetEntity();
            JsonObject message = new JsonObject().put("event", "playerJoined").put("player", player.getName())
                    .put("playerUUID", player.getUniqueId().toString());
            eventBusSender.send(message);
        });

        Sponge.getEventManager().registerListener(plugin, InteractBlockEvent.class, event -> {
            final Optional<ItemStackSnapshot> snapshot = event.getCause().getContext().get(EventContextKeys.USED_ITEM);
            snapshot.ifPresent(itemStackSnapshot -> {
                if (itemStackSnapshot.createGameDictionaryEntry().matches(LocationToolAction.locationEventCreateTool())) {
                    Player player = (Player) event.getSource();
                    event.getInteractionPoint().ifPresent(
                            handleLocationToolEvent(event, itemStackSnapshot, player)
                    );
                }
            });
        });
    }

    private Consumer<Vector3d> handleLocationToolEvent(InteractBlockEvent event, ItemStackSnapshot itemStackSnapshot, Player player) {
        return vector3d -> {
            final String locationName = itemStackSnapshot.createStack().get(Keys.ITEM_LORE)
                    .orElseThrow(IllegalArgumentException::new).get(0).toPlain();
            final String playerBoxLocation = player.getUniqueId() + locationName;

            final Location<World> eventLocation = new Location<>(player.getWorld(), vector3d);
            final Pair<Location<World>, Location<World>> locationPair;

            if (event instanceof InteractBlockEvent.Secondary) {
                locationPair = updatePlayerBoxLocation(playerBoxLocation, null, eventLocation);
                player.sendMessage(Text.of("second point set"));
            } else {
                locationPair = updatePlayerBoxLocation(playerBoxLocation, eventLocation, null);
                player.sendMessage(Text.of("first point set"));
            }

            if (locationPair.getLeft() != null && locationPair.getRight() != null) {
                final Condition condition = new LocatableInBoxCondition(player.getWorld(), locationPair);
                final String name = "player_inside_" + locationName + player.getUniqueId().toString();
                final Unregisterable unregisterable = conditionRegistrations.get(name);
                if (unregisterable != null) {
                    unregisterable.unregister();
                }
                ConditionServiceRegistration registration = conditionService.register(condition, (Player p) ->
                        eventBusSender.send(new JsonObject().put("event", name).put("playerUUID", p.getUniqueId().toString())));
                conditionRegistrations.put(name, registration);
            }
        };
    }

    private Pair<Location<World>, Location<World>> updatePlayerBoxLocation(String key, Location<World> locationLeft, Location<World> locationRight) {
        Pair<Location<World>, Location<World>> locationPair = playerBoxLocations.get(key);
        if (locationPair == null) {
            locationPair = Pair.of(locationLeft, locationRight);
        } else {
            locationPair = Pair.of(locationLeft != null ? locationLeft : locationPair.getLeft(),
                                   locationRight != null ? locationRight : locationPair.getRight());
        }

        playerBoxLocations.put(key, locationPair);
        return locationPair;
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
