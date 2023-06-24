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
package ch.vorburger.minecraft.storeys.japi.impl;

import ch.vorburger.minecraft.storeys.japi.Callback;
import ch.vorburger.minecraft.storeys.japi.Events;
import ch.vorburger.minecraft.storeys.japi.ReadingSpeed;
import ch.vorburger.minecraft.storeys.japi.Script;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionContextImpl;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionPlayer;
import ch.vorburger.minecraft.storeys.japi.impl.events.EventService;
import ch.vorburger.minecraft.storeys.japi.util.CommandExceptions;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.plugin.PluginContainer;

/**
 * {@link Events} implementation.
 * Created via {@link Scripts}.
 *
 * <p>The "lifecycle" of this is NOT a Singleton,
 * but one for each new (possibly reloaded) {@link Script} instance.
 */
class EventsImpl implements Events, Unregisterable {

    private static final Logger LOG = LoggerFactory.getLogger(EventsImpl.class);

    private final PluginContainer plugin;
    private final EventService eventService;

    // when made modifiable, then this should be per Player
    private final ReadingSpeed readingSpeed = new ReadingSpeed();

    private final Collection<Unregisterable> unregistrables = new ConcurrentLinkedQueue<>();
    private final ActionPlayer player = new ActionPlayer();

    EventsImpl(PluginContainer plugin, EventService eventService) {
        this.plugin = plugin;
        this.eventService = eventService;
    }

    @Override public void whenCommand(String name, Callback callback) {
        final Command.Parameterized spec = Command.builder().executor((src) -> {
            CommandExceptions.doOrThrow("/" + name, () -> invokeCallback(src.cause().audience(), callback));
            return CommandResult.success();
        }).build();
        final Optional<CommandRegistrar<Command.Parameterized>> registrar = Sponge.server().commandManager().registrar(Command.Parameterized.class);
        if (!registrar.isPresent()) {
            LOG.error("Could not register new command, because it's already present: /" + name);
            return;
        }
        registrar.get().register(plugin, spec, name);
        //TODO Sponge.server().commandManager().updateCommandTreeForPlayer();
        unregistrables.add(() -> {

        });
    }

    @Override public void whenPlayerJoins(Callback callback) {
        unregistrables.add(eventService.registerPlayerJoin(player -> {
            try {
                invokeCallback(player, callback);
            } catch (Exception e) {
                LOG.error("whenPlayerJoins() callback failure", e);
            }
        }));
    }

    @Override public void whenInside(String locationName, Callback callback) {
        unregistrables.add(eventService.registerInsideLocation(locationName, player -> {
            try {
                invokeCallback(player, callback);
            } catch (Exception e) {
                LOG.error("whenInside() callback failure", e);
            }
        }));
    }

    @Override public void whenEntityRightClicked(String entityName, Callback callback) {
        ch.vorburger.minecraft.storeys.japi.impl.events.Callback otherCallback = invoker -> {
            invokeCallback(invoker, callback);
        };
        unregistrables.add(eventService.registerInteractEntity(Component.text(entityName), otherCallback));
    }

    @Override public void unregister() {
        for (Unregisterable unregisterable : unregistrables) {
            unregisterable.unregister();
        }
    }

    private void invokeCallback(Audience source, Callback callback) throws Exception {
        MinecraftJvmImpl m = new MinecraftJvmImpl(plugin, source);
        callback.invoke(m);
        player.play(new ActionContextImpl(source, readingSpeed), m.getActionList());
    }
}
