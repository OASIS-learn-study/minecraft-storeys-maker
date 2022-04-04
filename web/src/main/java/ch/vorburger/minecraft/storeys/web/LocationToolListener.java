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
import ch.vorburger.minecraft.storeys.japi.impl.Unregisterable;
import ch.vorburger.minecraft.storeys.model.LocationToolAction;
import ch.vorburger.minecraft.storeys.web.location.LocationHitBox;
import ch.vorburger.minecraft.storeys.web.location.LocationPairSerializer;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Singleton
public class LocationToolListener {
    private final Map<String, Pair<Location<World>, Location<World>>> playerBoxLocations = new ConcurrentHashMap<>();
    private final Map<String, Unregisterable> conditionRegistrations = new ConcurrentHashMap<>();
    private final EventBusSender eventBusSender;
    private final ConditionService conditionService;
    private final ConfigurationLoader<CommentedConfigurationNode> configurationLoader;

    @Inject public LocationToolListener(PluginInstance plugin, EventManager eventManager, EventBusSender eventBusSender,
            ConditionService conditionService, ConfigurationLoader<CommentedConfigurationNode> loader) {
        TypeSerializers.getDefaultSerializers().registerType(LocationPairSerializer.TYPE, new LocationPairSerializer());
        eventManager.registerListeners(plugin, this);
        this.eventBusSender = eventBusSender;
        this.conditionService = conditionService;
        this.configurationLoader = loader;

        try {
            ConfigurationNode rootNode = configurationLoader.load();
            List<LocationHitBox> hitBoxList = new ArrayList<>(rootNode.getNode("locations").getList(LocationHitBox.TYPE));
            for (LocationHitBox hitBox : hitBoxList) {
                registerCondition(hitBox.getPlayerUUID(), hitBox.getName(), hitBox.getBox());
            }
        } catch (IOException | ObjectMappingException e) {
            throw new RuntimeException("could not load locations from config", e);
        }
    }

    @Listener public void locationToolInteraction(InteractBlockEvent event) {
        final Optional<ItemStackSnapshot> snapshot = event.getCause().getContext().get(EventContextKeys.USED_ITEM);
        snapshot.ifPresent(itemStackSnapshot -> {
            if (itemStackSnapshot.createGameDictionaryEntry().matches(LocationToolAction.locationEventCreateTool())) {
                Player player = (Player) event.getSource();
                event.getInteractionPoint().ifPresent(handleLocationToolEvent(event, itemStackSnapshot, player));
            }
        });
    }

    @Listener public void onGameStoppingServer(GameStoppingServerEvent event) throws Exception {
        for (Unregisterable value : conditionRegistrations.values()) {
            value.unregister();
        }
    }

    private Consumer<Vector3d> handleLocationToolEvent(InteractBlockEvent event, ItemStackSnapshot itemStackSnapshot, Player player) {
        return vector3d -> {
            final String locationName = itemStackSnapshot.createStack().get(Keys.ITEM_LORE).orElseThrow(IllegalArgumentException::new)
                    .get(0).toPlain();
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
                saveLocation(locationPair, player, locationName);
                registerCondition(player.getUniqueId(), locationName, locationPair);
            }
        };
    }

    private void registerCondition(UUID player, String locationName, Pair<Location<World>, Location<World>> locationPair) {
        final Condition condition = new LocatableInBoxCondition(locationPair);
        final String name = "player_inside_" + locationName + player.toString();
        final Unregisterable unregisterable = conditionRegistrations.get(name);
        if (unregisterable != null) {
            unregisterable.unregister();
        }
        ConditionService.ConditionServiceRegistration registration = conditionService.register(condition,
                (Player p) -> eventBusSender.send(new JsonObject().put("event", name).put("playerUUID", p.getUniqueId().toString())));
        conditionRegistrations.put(name, registration);
    }

    private void saveLocation(Pair<Location<World>, Location<World>> locationPair, Player player, String locationName) {
        try {
            ConfigurationNode rootNode = configurationLoader.load();
            List<LocationHitBox> hitBoxList = new ArrayList<>(rootNode.getNode("locations").getList(LocationHitBox.TYPE));
            hitBoxList.add(new LocationHitBox(player.getUniqueId(), locationName, locationPair));
            rootNode.getNode("locations").setValue(new TypeToken<List<LocationHitBox>>() {
            }, hitBoxList);
            configurationLoader.save(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("could not save into config...", e);
        }
    }

    private Pair<Location<World>, Location<World>> updatePlayerBoxLocation(String key, Location<World> locationLeft,
            Location<World> locationRight) {
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
