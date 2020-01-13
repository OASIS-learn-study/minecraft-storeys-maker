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

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.events.Condition;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.events.LocatableInBoxCondition;
import ch.vorburger.minecraft.storeys.events.Unregisterable;
import ch.vorburger.minecraft.storeys.model.LocationToolAction;
import com.flowpowered.math.vector.Vector3d;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Singleton
public class LocationToolListener {
    private final Map<String, Pair<Location<World>, Location<World>>> playerBoxLocations = new ConcurrentHashMap<>();
    private final Map<String, Unregisterable> conditionRegistrations = new ConcurrentHashMap<>();
    private final EventBusSender eventBusSender;
    private final ConditionService conditionService;

    @Inject
    public LocationToolListener(PluginInstance plugin, EventBusSender eventBusSender, ConditionService conditionService) {
        Sponge.getEventManager().registerListeners(plugin, this);
        this.eventBusSender = eventBusSender;
        this.conditionService = conditionService;
    }

    @Listener
    public void locationToolInteraction(InteractBlockEvent event) {
        final Optional<ItemStackSnapshot> snapshot = event.getCause().getContext().get(EventContextKeys.USED_ITEM);
        snapshot.ifPresent(itemStackSnapshot -> {
            if (itemStackSnapshot.createGameDictionaryEntry().matches(LocationToolAction.locationEventCreateTool())) {
                Player player = (Player) event.getSource();
                event.getInteractionPoint().ifPresent(
                        handleLocationToolEvent(event, itemStackSnapshot, player)
                );
            }
        });
    }

    @Listener
    public void onGameStoppingServer(GameStoppingServerEvent event) throws Exception {
        for (Unregisterable value : conditionRegistrations.values()) {
            value.unregister();
        }
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
                ConditionService.ConditionServiceRegistration registration = conditionService.register(condition, (Player p) ->
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
}
