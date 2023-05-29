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

import ch.vorburger.minecraft.storeys.japi.Action;
import ch.vorburger.minecraft.storeys.japi.Events;
import ch.vorburger.minecraft.storeys.japi.Minecraft;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionWaitHelper;
import ch.vorburger.minecraft.storeys.japi.impl.actions.CommandAction;
import ch.vorburger.minecraft.storeys.japi.impl.actions.NarrateAction;
import ch.vorburger.minecraft.storeys.japi.impl.actions.Narrator;
import ch.vorburger.minecraft.storeys.japi.impl.actions.TitleAction;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.plugin.PluginContainer;

/**
 * {@link Minecraft} implementation.
 * Created indirectly (by EventsImpl) via {@link Scripts}.
 *
 * <p>The "lifecycle" of this is NOT a Singleton,
 * but one for each instance (not just kind of) of an event registered on {@link Events},
 * such as custom command, when right-clicked, when player joined, when inside, etc.
 */
class MinecraftJvmImpl implements Minecraft {

    private final PluginContainer plugin;
    private final Audience source;
    private final ActionWaitHelper actionWaitHelper;

    private final List<Action<?>> actionList = new ArrayList<>();

    MinecraftJvmImpl(PluginContainer plugin, Audience source) {
        this.source = source;
        this.plugin = plugin;
        this.actionWaitHelper = new ActionWaitHelper(plugin);
    }

    @Override public void cmd(String command) {
        CommandAction action = new CommandAction(plugin, Sponge.asyncScheduler());
        action.setCommand(command);
        actionList.add(action);
    }

    @Override public void title(String text) {
        TitleAction action = new TitleAction(actionWaitHelper);
        action.setParameter(text);
        actionList.add(action);
    }

    @Override public void narrate(String entity, String text) {
        Narrator narrator = new Narrator(plugin);
        NarrateAction action = new NarrateAction(narrator);
        action.setEntity(entity);
        action.setText(Component.text(text));
        actionList.add(action);
    }

    @Override public Player player() {
        // TODO if (source instanceof Player), else... error handling TBD (not just log, but explain it to source)
        return (Player) source;
    }

    public List<Action<?>> getActionList() {
        return actionList;
    }
}
