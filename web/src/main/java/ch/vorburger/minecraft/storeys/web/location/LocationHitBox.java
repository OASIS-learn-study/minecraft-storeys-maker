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

import io.leangen.geantyref.TypeToken;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class LocationHitBox {
    public static TypeToken<LocationHitBox> TYPE = new TypeToken<LocationHitBox>(){};

    @Setting(value = "player") private UUID playerUUID;

    @Setting(value = "name") private String name;

    @Setting(value = "hitbox") private Pair<ServerLocation, ServerLocation> box;

    public LocationHitBox() {
    }

    public LocationHitBox(UUID playerUUID, String name, Pair<ServerLocation, ServerLocation> box) {
        this.playerUUID = playerUUID;
        this.name = name;
        this.box = box;
    }

    public Pair<ServerLocation, ServerLocation> getBox() {
        return box;
    }

    public String getName() {
        return name;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
