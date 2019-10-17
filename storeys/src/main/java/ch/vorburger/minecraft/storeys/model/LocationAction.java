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

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.events.LocatableInBoxCondition;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Locatable;

public class LocationAction implements Action<Void> {
    private static ConditionService conditionService;

    private String coordinates;

    public LocationAction(PluginInstance plugin) {
        if (conditionService == null) {
            conditionService = new ConditionService(plugin);
        }
    }

    public Action<?> setBox(String coordinates) {
        this.coordinates = coordinates;
        return this;
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
}
