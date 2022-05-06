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

import static java.util.Objects.requireNonNull;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.japi.Events;
import ch.vorburger.minecraft.storeys.japi.Minecraft;
import ch.vorburger.minecraft.storeys.japi.impl.actions.Action;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionContext;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionWaitHelper;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ReadingSpeed;
import ch.vorburger.minecraft.storeys.japi.impl.actions.TitleAction;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

/**
 * {@link Minecraft} implementation.
 * Created indirectly (by EventsImpl) via {@link Scripts}.
 *
 * <p>The "lifecycle" of this is NOT a Singleton,
 * but one for each instance (not just kind of) of an event registered on {@link Events},
 * such as custom command, when right clicked, when player joined, when inside, etc.
 */
class MinecraftJvmImpl implements Minecraft {

    private static final Logger LOG = LoggerFactory.getLogger(EventsImpl.class);

    private final CommandSource source;
    private final ActionWaitHelper actionWaitHelper;
    private final ReadingSpeed readingSpeed = new ReadingSpeed();

    MinecraftJvmImpl(CommandSource source, PluginInstance plugin) {
        this.source = source;
        this.actionWaitHelper = new ActionWaitHelper(plugin);
    }

    private void exec(Action<Void> action) {
        ActionContext ctx = new ActionContext(source, readingSpeed);
        try {
            action.execute(ctx).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Awaiting action execuition failed", e);
        }
    }

    // TODO exec(new CommandAction()) instead
    @Override public void cmd(String command) {
        String commandWithoutSlash = requireNonNull(command, "command").trim();
        if (commandWithoutSlash.startsWith("/")) {
            commandWithoutSlash = commandWithoutSlash.substring(1);
        }
        Sponge.getCommandManager().process(source, requireNonNull(commandWithoutSlash, "commandWithoutSlash"));
        // returned CommandResult is ignored
    }

    @Override public void title(String text) {
        TitleAction action = new TitleAction(actionWaitHelper);
        action.setParameter(text);
        exec(action);
    }

    @Override public Player player() {
        // TODO if (source instanceof Player), else... error handling TBD (not just log, but explain it to source)
        return (Player) source;
    }
}
