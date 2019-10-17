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
package ch.vorburger.minecraft.storeys.model;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.events.LocatableInBoxCondition;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Locatable;

import javax.inject.Inject;

public class LocationAction implements Action<Void> {
    private ConditionService conditionService;
    private String coordinates;

    public LocationAction() {}

    @Inject
    public LocationAction(PluginInstance plugin) {
        conditionService = new ConditionService(plugin);
    }

    @Override
    public void setParameter(String param) {
        this.coordinates = param;
    }

    @Override
    public Pattern getPattern() {
        return Pattern.compile("^%in\\s([^\\n]*)");
    }

    @Override
    public CompletionStage<Void> execute(ActionContext context) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            Locatable locatable = (Locatable) context.getCommandSource();
            LocatableInBoxCondition condition = new LocatableInBoxCondition(locatable.getWorld(), coordinates);

            conditionService.register(condition, (Player p) -> future.complete(null));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + coordinates;
    }
}
