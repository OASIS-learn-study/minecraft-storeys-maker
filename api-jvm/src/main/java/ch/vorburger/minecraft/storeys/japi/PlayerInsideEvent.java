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
package ch.vorburger.minecraft.storeys.japi;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public class PlayerInsideEvent extends AbstractEvent implements Cancellable {
    private final Player player;
    private final Cause cause;
    private final String locationName;
    private boolean cancelled = false;

    public PlayerInsideEvent(Player player, String locationName, Cause cause) {
        this.player = player;
        this.locationName = locationName;
        this.cause = cause;
    }

    @Override public Cause cause() {
        return cause;
    }

    @Override public boolean isCancelled() {
        return cancelled;
    }

    @Override public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public Player getEffectedPlayer() {
        return this.player;
    }

    public String getLocationName() {
        return locationName;
    }

    @Override public String toString() {
        return "PlayerInsideEvent{" + "player=" + player + ", locationName='" + locationName + '\'' + '}';
    }
}
