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
package ch.vorburger.minecraft.storeys.events;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.text.Text;

public class EventService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    private final AtomicReference<Consumer<Join>> onPlayerJoinCallback = new AtomicReference<>();
    private final Map<String, Runnable> onInteractEntityEventCallbacks = new ConcurrentHashMap<>();

    public EventService(PluginInstance plugin) {
    }

    @Override
    public void close() throws Exception {
    }

    public void registerPlayerJoin(Consumer<Join> callback) {
        if (!onPlayerJoinCallback.compareAndSet(null, callback)) {
            throw new IllegalStateException("Only 1 onPlayerJoin Callback supported");
        }
    }

    public void onPlayerJoin(Join event) throws Exception {
        Consumer<Join> callback = onPlayerJoinCallback.get();
        if (callback != null) {
            callback.accept(event);
        }
    }

    public Unregisterable registerInteractEntity(String entityName, Runnable callback) {
        onInteractEntityEventCallbacks.putIfAbsent(entityName, callback);
        return () -> onInteractEntityEventCallbacks.remove(entityName);
    }

    public void onInteractEntityEvent(InteractEntityEvent event) {
        // TODO This is bad, it means that entities are only recognized by name if they are not narrating..
        Optional<Text> optEntityNameText = event.getTargetEntity().get(Keys.DISPLAY_NAME);
        LOG.info("InteractEntityEvent: entityName={}; event={}", optEntityNameText, event);
        optEntityNameText.ifPresent(entityNameText ->
            onInteractEntityEventCallbacks.getOrDefault(entityNameText.toPlain(), () -> {}).run());
    }

    public void onChangeInventoryHeldEvent(ChangeInventoryEvent.Held event) {
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

}
