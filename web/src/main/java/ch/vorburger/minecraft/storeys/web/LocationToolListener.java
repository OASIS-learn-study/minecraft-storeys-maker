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

import static ch.vorburger.minecraft.storeys.model.LocationToolAction.isLocationToolStack;

import ch.vorburger.minecraft.storeys.events.Condition;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.events.LocatableInBoxCondition;
import ch.vorburger.minecraft.storeys.japi.PlayerInsideEvent;
import ch.vorburger.minecraft.storeys.japi.impl.Unregisterable;
import ch.vorburger.minecraft.storeys.japi.util.ComponentTexts;
import ch.vorburger.minecraft.storeys.plugin.PluginInstance;
import ch.vorburger.minecraft.storeys.web.location.LocationHitBox;
import ch.vorburger.minecraft.storeys.web.location.LocationPairSerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Server;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppedGameEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

@Singleton public class LocationToolListener {
    private static final Logger LOG = LoggerFactory.getLogger(LocationToolListener.class);
    private static final LocationPairSerializer PAIR_SERIALIZER = new LocationPairSerializer();

    private final Map<String, Pair<ServerLocation, ServerLocation>> playerBoxLocations = new ConcurrentHashMap<>();
    private final Map<String, Unregisterable> conditionRegistrations = new ConcurrentHashMap<>();
    private final ConditionService conditionService;
    private final ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
    private final ConfigurationOptions locationConfigOptions;

    private final EventContext eventContext;
    private EventManager eventManager;
    private final PluginInstance plugin;
    private volatile boolean savedLocationsLoaded;

    @Inject public LocationToolListener(PluginInstance plugin, EventManager eventManager, ConditionService conditionService,
            ConfigurationLoader<CommentedConfigurationNode> loader) {
        final TypeSerializerCollection serializers = TypeSerializerCollection.builder()
                .registerAll(loader.defaultOptions().serializers())
                .register(LocationPairSerializer.TYPE, new LocationPairSerializer())
                .build();
        this.locationConfigOptions = loader.defaultOptions().serializers(serializers);
        eventManager.registerListeners(plugin.getPluginContainer(), this);
        LOG.info("Registered location tool block interaction listener");
        this.eventManager = eventManager;
        this.plugin = plugin;
        this.eventContext = EventContext.builder().add(EventContextKeys.PLUGIN, plugin.getPluginContainer()).build();
        this.conditionService = conditionService;
        configurationLoader = loader;
    }

    /** Worlds are not available during {@link StartingEngineEvent}; load regions once the engine has started. */
    @Listener public void onServerStarted(StartedEngineEvent<Server> event) {
        if (savedLocationsLoaded) {
            return;
        }
        savedLocationsLoaded = true;
        loadSavedLocationsFromConfig();
    }

    /**
     * Single listener on the base event (as on Sponge 7). Do not use {@code @First ServerPlayer} here:
     * if the player is not first in the cause chain, Sponge skips the handler entirely with no log output.
     */
    @Listener public void onLocationToolInteractBlock(InteractBlockEvent event) {
        final Optional<ServerPlayer> optPlayer = event.cause().first(ServerPlayer.class);
        if (!optPlayer.isPresent()) {
            return;
        }
        final ServerPlayer player = optPlayer.get();
        final ItemStack itemStack = player.itemInHand(HandTypes.MAIN_HAND);
        if (!isLocationToolStack(itemStack)) {
            return;
        }
        final Optional<String> locationName = locationNameFrom(itemStack);
        if (!locationName.isPresent()) {
            LOG.warn("Location tool used without region name in lore");
            return;
        }

        final boolean secondPoint;
        if (event instanceof InteractBlockEvent.Secondary) {
            secondPoint = true;
        } else if (event instanceof InteractBlockEvent.Primary.Start) {
            secondPoint = false;
        } else {
            return;
        }

        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }

        final ServerLocation eventLocation = blockLocation(event, player);
        final String playerBoxLocation = player.identity().uuid() + locationName.get();
        final Pair<ServerLocation, ServerLocation> locationPair;
        if (secondPoint) {
            locationPair = updatePlayerBoxLocation(playerBoxLocation, null, eventLocation);
            player.sendMessage(Component.text("second point set"));
            LOG.info("Location tool: second point for '{}' at {}", locationName.get(), eventLocation.position());
        } else {
            locationPair = updatePlayerBoxLocation(playerBoxLocation, eventLocation, null);
            player.sendMessage(Component.text("first point set"));
            LOG.info("Location tool: first point for '{}' at {}", locationName.get(), eventLocation.position());
        }

        if (locationPair.getLeft() != null && locationPair.getRight() != null) {
            saveLocation(locationPair, player, locationName.get());
            registerCondition(player.identity().uuid(), locationName.get(), locationPair);
            LOG.info("Location tool: region '{}' saved", locationName.get());
        }
    }

    @Listener public void onGameStoppingServer(StoppedGameEvent event) throws Exception {
        for (Unregisterable value : conditionRegistrations.values()) {
            value.unregister();
        }
    }

    private static ServerLocation blockLocation(InteractBlockEvent event, ServerPlayer player) {
        final ServerWorld world = player.serverLocation().world();
        if (event instanceof InteractBlockEvent.Secondary) {
            final Vector3d point = ((InteractBlockEvent.Secondary) event).interactionPoint();
            return ServerLocation.of(world, blockPos(point));
        }
        return event.block().location()
                .orElseGet(() -> ServerLocation.of(world, event.block().position()));
    }

    private static Vector3i blockPos(Vector3d point) {
        return Vector3i.from((int) Math.floor(point.x()), (int) Math.floor(point.y()), (int) Math.floor(point.z()));
    }

    private static Optional<String> locationNameFrom(ItemStack itemStack) {
        return itemStack.get(Keys.LORE)
                .filter(lore -> !lore.isEmpty())
                .map(lore -> ComponentTexts.plainText(lore.get(0)));
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

    private void loadSavedLocationsFromConfig() {
        try {
            final ConfigurationNode rootNode = configurationLoader.load(locationConfigOptions);
            int loaded = 0;
            for (LocationHitBox hitBox : loadSavedLocations(rootNode)) {
                registerCondition(hitBox.getPlayerUUID(), hitBox.getName(), hitBox.getBox());
                loaded++;
            }
            if (loaded > 0) {
                LOG.info("Loaded {} saved location region(s) from config", loaded);
            }
        } catch (SerializationException e) {
            LOG.error("Could not load locations from config", e);
        } catch (IOException e) {
            LOG.error("Could not load locations from config", e);
        }
    }

    private List<LocationHitBox> loadSavedLocations(ConfigurationNode rootNode) {
        final ConfigurationNode locationsNode = rootNode.node("locations");
        if (locationsNode.virtual() || !locationsNode.isList()) {
            return new ArrayList<>();
        }
        final List<LocationHitBox> result = new ArrayList<>();
        for (ConfigurationNode entry : locationsNode.childrenList()) {
            final String playerUuidStr = entry.node("player").getString();
            final String name = entry.node("name").getString();
            if (playerUuidStr == null || name == null) {
                LOG.warn("Skipping invalid location entry in config: {}", entry);
                continue;
            }
            try {
                final Pair<ServerLocation, ServerLocation> box = PAIR_SERIALIZER.deserialize(null, entry.node("hitbox"));
                result.add(new LocationHitBox(UUID.fromString(playerUuidStr), name, box));
            } catch (SerializationException e) {
                LOG.warn("Skipping location '{}' in config: {}", name, e.getMessage());
            }
        }
        return result;
    }

    private void writeLocations(ConfigurationNode rootNode, List<LocationHitBox> hitBoxList) throws SerializationException {
        final ConfigurationNode locationsNode = rootNode.node("locations");
        if (locationsNode.isList()) {
            while (!locationsNode.empty()) {
                locationsNode.removeChild(0);
            }
        }
        for (LocationHitBox hitBox : hitBoxList) {
            final ConfigurationNode entry = locationsNode.appendListNode();
            entry.node("player").set(hitBox.getPlayerUUID().toString());
            entry.node("name").set(hitBox.getName());
            PAIR_SERIALIZER.serialize(null, hitBox.getBox(), entry.node("hitbox"));
        }
    }

    private void saveLocation(Pair<ServerLocation, ServerLocation> locationPair, Player player, String locationName) {
        try {
            final ConfigurationNode rootNode = configurationLoader.load(locationConfigOptions);
            final List<LocationHitBox> hitBoxList = loadSavedLocations(rootNode);
            hitBoxList.add(new LocationHitBox(player.identity().uuid(), locationName, locationPair));
            writeLocations(rootNode, hitBoxList);
            configurationLoader.save(rootNode);
            LOG.info("Location tool: saved region '{}' to config", locationName);
        } catch (SerializationException e) {
            throw new RuntimeException("could not save into config...", e);
        } catch (IOException e) {
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
