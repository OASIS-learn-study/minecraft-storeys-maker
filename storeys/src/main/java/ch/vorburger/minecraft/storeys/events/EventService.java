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
package ch.vorburger.minecraft.storeys.events;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.japi.impl.Unregisterable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;
import org.spongepowered.api.text.Text;

public class EventService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    private final AtomicReference<Consumer<Join>> onPlayerJoinCallback = new AtomicReference<>();
    private final Map<String, Callback> onInteractEntityEventCallbacks = new ConcurrentHashMap<>();

    @Inject public EventService(PluginInstance plugin, EventManager eventManager) {
        // TODO Other Event registrations should later go up into AbstractStoreysPlugin so that Script can have Event triggers
        // as well, but for now:
        eventManager.registerListeners(plugin, this);
        // InteractItemEvent ?
    }

    @Override public void close() throws Exception {
    }

    public void registerPlayerJoin(Consumer<Join> callback) {
        if (!onPlayerJoinCallback.compareAndSet(null, callback)) {
            throw new IllegalStateException("Only 1 onPlayerJoin Callback supported");
        }
    }

    @Listener public void onPlayerJoin(Join event) throws Exception {
        Consumer<Join> callback = onPlayerJoinCallback.get();
        if (callback != null) {
            callback.accept(event);
        }
    }

    public Unregisterable registerInteractEntity(String entityName, Callback callback) {
        onInteractEntityEventCallbacks.putIfAbsent(entityName, callback);
        return () -> onInteractEntityEventCallbacks.remove(entityName);
    }

    @Listener public void onInteractEntityEvent(InteractEntityEvent event) {
        // TODO This is bad, it means that entities are only recognized by name if they are not narrating..
        Optional<Text> optEntityNameText = event.getTargetEntity().get(Keys.DISPLAY_NAME);
        LOG.debug("InteractEntityEvent: entityName={}; event={}", optEntityNameText, event);
        optEntityNameText.ifPresent(entityNameText -> {
            Callback callback = onInteractEntityEventCallbacks.getOrDefault(entityNameText.toPlain(), (Player player) -> {
            });
            if (callback != null) {
                try {
                    callback.call(event.getCause().last(Player.class).orElse(null));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Listener public void onChangeInventoryHeldEvent(ChangeInventoryEvent.Held event) {
        /* LOG.info("onChangeInventory event={}", event);
         * LOG.info("onChangeInventory event={}", event.getTargetInventory().first().toString());
         * Optional<Player> optPlayer = event.getCause().first(Player.class);
         * optPlayer.ifPresent(player -> {
         * // TODO https://github.com/vorburger/minecraft-storeys-maker/issues/34: Only get() ifPresent()
         * LOG.info("onChangeInventory item.id={}", player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getId());
         * LOG.info("onChangeInventory item.name={}", player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getName());
         * LOG.info("onChangeInventory item.type.id={}",
         * player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getType().getId());
         * LOG.info("onChangeInventory item.type.name={}",
         * player.getItemInHand(HandTypes.MAIN_HAND).get().getItem().getType().getName());
         * }); */
    }

}
