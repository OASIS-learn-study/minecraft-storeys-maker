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

import ch.vorburger.minecraft.storeys.events.Condition;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.events.LocatableInBoxCondition;
import ch.vorburger.minecraft.storeys.japi.PlayerInsideEvent;
import ch.vorburger.minecraft.storeys.japi.impl.Unregisterable;
import ch.vorburger.minecraft.storeys.model.LocationToolAction;
import ch.vorburger.minecraft.storeys.plugin.PluginInstance;
import ch.vorburger.minecraft.storeys.web.location.LocationHitBox;
import ch.vorburger.minecraft.storeys.web.location.LocationPairSerializer;
import io.leangen.geantyref.TypeToken;
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
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.lifecycle.StoppedGameEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.math.vector.Vector3i;

@Singleton public class LocationToolListener {
    private static final Logger LOG = LoggerFactory.getLogger(LocationToolListener.class);
    private final Map<String, Pair<ServerLocation, ServerLocation>> playerBoxLocations = new ConcurrentHashMap<>();
    private final Map<String, Unregisterable> conditionRegistrations = new ConcurrentHashMap<>();
    private final ConditionService conditionService;
    private final ConfigurationLoader<CommentedConfigurationNode> configurationLoader;

    private final EventContext eventContext;
    private EventManager eventManager;
    private final PluginInstance plugin;

    @Inject public LocationToolListener(PluginInstance plugin, EventManager eventManager, ConditionService conditionService,
            ConfigurationLoader<CommentedConfigurationNode> loader) {
        TypeSerializerCollection.builder().register(LocationPairSerializer.TYPE, new LocationPairSerializer());
        eventManager.registerListeners(plugin.getPluginContainer(), this);
        this.eventManager = eventManager;
        this.plugin = plugin;
        this.eventContext = EventContext.builder().add(EventContextKeys.PLUGIN, plugin.getPluginContainer()).build();
        this.conditionService = conditionService;
        configurationLoader = loader;

        try {
            ConfigurationNode rootNode = configurationLoader.load();
            List<LocationHitBox> hitBoxList = new ArrayList<>(rootNode.node("locations").getList(LocationHitBox.TYPE));
            for (LocationHitBox hitBox : hitBoxList) {
                registerCondition(hitBox.getPlayerUUID(), hitBox.getName(), hitBox.getBox());
            }
        } catch (IOException e) {
            throw new RuntimeException("could not load locations from config", e);
        }
    }

    @Listener public void locationToolInteraction(InteractBlockEvent.Primary event, @First ServerPlayer player) {
        final Optional<ItemStackSnapshot> snapshot = event.context().get(EventContextKeys.USED_ITEM);
        snapshot.ifPresent(itemStackSnapshot -> {
            final ItemStack itemStack = player.itemInHand(HandTypes.MAIN_HAND);
            if (itemStack.equals(LocationToolAction.locationEventCreateTool())) {
                handleLocationToolEvent(event, itemStackSnapshot, player);
            }
        });
    }

    @Listener public void onGameStoppingServer(StoppedGameEvent event) throws Exception {
        for (Unregisterable value : conditionRegistrations.values()) {
            value.unregister();
        }
    }

    private Consumer<Vector3i> handleLocationToolEvent(InteractBlockEvent event, ItemStackSnapshot itemStackSnapshot, Player player) {
        return vector3d -> {
            final String locationName = itemStackSnapshot.createStack().get(Keys.LORE).orElseThrow(IllegalArgumentException::new)
                    .get(0).examinableName();
            final String playerBoxLocation = player.identity().uuid() + locationName;

            final ServerLocation eventLocation = ServerLocation.of(player.serverLocation().world(), vector3d);
            final Pair<ServerLocation, ServerLocation> locationPair;

            if (event instanceof InteractBlockEvent.Secondary) {
                locationPair = updatePlayerBoxLocation(playerBoxLocation, null, eventLocation);
                player.sendMessage(Component.text("second point set"));
            } else {
                locationPair = updatePlayerBoxLocation(playerBoxLocation, eventLocation, null);
                player.sendMessage(Component.text("first point set"));
            }

            if (locationPair.getLeft() != null && locationPair.getRight() != null) {
                saveLocation(locationPair, player, locationName);
                registerCondition(player.identity().uuid(), locationName, locationPair);
            }
        };
    }

    private void registerCondition(UUID player, String locationName, Pair<ServerLocation, ServerLocation> locationPair) {
        final Condition condition = new LocatableInBoxCondition(locationPair);
        final String name = "player_inside_" + locationName + player.toString();
        final Unregisterable unregisterable = conditionRegistrations.get(name);
        if (unregisterable != null) {
            unregisterable.unregister();
        }
        ConditionService.ConditionServiceRegistration registration = conditionService.register(condition, (p) -> {
            eventManager.post(new PlayerInsideEvent(p, locationName, Cause.of(eventContext, plugin)));
        });
        conditionRegistrations.put(name, registration);
    }

    @SuppressWarnings("serial") private void saveLocation(Pair<ServerLocation, ServerLocation> locationPair, Player player,
            String locationName) {
        try {
            ConfigurationNode rootNode = configurationLoader.load();
            List<LocationHitBox> hitBoxList = new ArrayList<>(rootNode.node("locations").getList(LocationHitBox.TYPE));
            hitBoxList.add(new LocationHitBox(player.identity().uuid(), locationName, locationPair));
            rootNode.node("locations").set(TypeToken.get(List.class), hitBoxList);
            configurationLoader.save(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("could not save into config...", e);
        }
    }

    private Pair<ServerLocation, ServerLocation> updatePlayerBoxLocation(String key, ServerLocation locationLeft,
            ServerLocation locationRight) {
        Pair<ServerLocation, ServerLocation> locationPair = playerBoxLocations.get(key);
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
