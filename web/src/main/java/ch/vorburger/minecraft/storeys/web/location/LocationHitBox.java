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
package ch.vorburger.minecraft.storeys.web.location;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

@ConfigSerializable
public class LocationHitBox {
    public static TypeToken<LocationHitBox> TYPE = TypeToken.of(LocationHitBox.class);

    @Setting(value="player")
    private UUID playerUUID;

    @Setting(value="name")
    private String name;

    @Setting(value="hitbox")
    private Pair<Location<World>, Location<World>> box;

    public LocationHitBox() {
    }

    public LocationHitBox(UUID playerUUID, String name, Pair<Location<World>, Location<World>> box) {
        this.playerUUID = playerUUID;
        this.name = name;
        this.box = box;
    }

    public Pair<Location<World>, Location<World>> getBox() {
        return box;
    }

    public String getName() {
        return name;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
