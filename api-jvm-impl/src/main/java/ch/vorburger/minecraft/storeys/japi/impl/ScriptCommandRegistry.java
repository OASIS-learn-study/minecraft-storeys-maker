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

import ch.vorburger.minecraft.storeys.japi.util.CommandExceptions;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.kyori.adventure.audience.Audience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.manager.CommandFailedRegistrationException;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.plugin.PluginContainer;

/**
 * Registers each command alias once with Sponge and dispatches to per-script handlers
 * that can be added and removed on script reload.
 */
@Singleton
public class ScriptCommandRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ScriptCommandRegistry.class);

    private final PluginContainer plugin;
    private final Map<String, AliasHandlers> aliases = new ConcurrentHashMap<>();

    @Inject
    public ScriptCommandRegistry(PluginContainer plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers a handler for the given alias. The first registration for an alias
     * registers the command with Sponge; later registrations only add callbacks.
     */
    public Unregisterable register(Object owner, String alias, CommandHandler handler) {
        AliasHandlers handlers = aliases.computeIfAbsent(alias, a -> new AliasHandlers());
        handlers.add(owner, handler);
        ensureSpongeRegistered(alias, handlers);
        return () -> handlers.remove(owner, handler);
    }

    private void ensureSpongeRegistered(String alias, AliasHandlers handlers) {
        if (handlers.isSpongeRegistered()) {
            return;
        }
        synchronized (handlers) {
            if (handlers.isSpongeRegistered()) {
                return;
            }
            final Optional<CommandRegistrar<Command.Parameterized>> registrar =
                    Sponge.server().commandManager().registrar(Command.Parameterized.class);
            if (!registrar.isPresent()) {
                LOG.error("No Command.Parameterized registrar available; cannot register /{}", alias);
                return;
            }
            final Command.Parameterized spec = Command.builder().executor((src) -> {
                final Audience audience = src.cause().audience();
                CommandExceptions.doOrThrow("/" + alias, () -> handlers.dispatch(audience));
                return CommandResult.success();
            }).build();
            try {
                registrar.get().register(plugin, spec, alias);
                handlers.setSpongeRegistered(true);
                Sponge.server().onlinePlayers().forEach(
                        p -> Sponge.server().commandManager().updateCommandTreeForPlayer(p));
                LOG.debug("Registered dynamic command /{}", alias);
            } catch (CommandFailedRegistrationException e) {
                LOG.error("Failed to register command /{} with Sponge", alias, e);
            }
        }
    }

    @FunctionalInterface
    public interface CommandHandler {
        void execute(Audience source) throws Exception;
    }

    private static final class AliasHandlers {
        private final Map<Object, Collection<CommandHandler>> byOwner = new ConcurrentHashMap<>();
        private volatile boolean spongeRegistered;

        boolean isSpongeRegistered() {
            return spongeRegistered;
        }

        void setSpongeRegistered(boolean spongeRegistered) {
            this.spongeRegistered = spongeRegistered;
        }

        void add(Object owner, CommandHandler handler) {
            byOwner.computeIfAbsent(owner, k -> new ConcurrentLinkedQueue<>()).add(handler);
        }

        void remove(Object owner, CommandHandler handler) {
            final Collection<CommandHandler> handlers = byOwner.get(owner);
            if (handlers != null) {
                handlers.remove(handler);
                if (handlers.isEmpty()) {
                    byOwner.remove(owner);
                }
            }
        }

        void dispatch(Audience audience) throws Exception {
            for (Collection<CommandHandler> handlers : byOwner.values()) {
                for (CommandHandler handler : handlers) {
                    handler.execute(audience);
                }
            }
        }
    }
}
