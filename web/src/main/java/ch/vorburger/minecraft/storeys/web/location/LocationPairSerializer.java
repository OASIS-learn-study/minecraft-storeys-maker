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
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class LocationPairSerializer implements TypeSerializer<Pair<Location<World>, Location<World>>> {
    public static TypeToken<Pair<Location<World>, Location<World>>> TYPE = new TypeToken<Pair<Location<World>, Location<World>>>(){};

    @Override
    public Pair<Location<World>, Location<World>> deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        final String worldName = value.getNode("worldName").getString();
        final int x1 = value.getNode("x1").getInt();
        final int y1 = value.getNode("y1").getInt();
        final int z1 = value.getNode("z1").getInt();

        final int x2 = value.getNode("x2").getInt();
        final int y2 = value.getNode("y2").getInt();
        final int z2 = value.getNode("z2").getInt();

        World world = Sponge.getServer().getWorld(worldName).orElseThrow(() -> new RuntimeException("world not initialized?"));
        final Location<World> point1 = new Location<>(world, x1, y1, z1);
        final Location<World> point2 = new Location<>(world, x2, y2, z2);
        return Pair.of(point1, point2);
    }

    @Override
    public void serialize(TypeToken<?> type, Pair<Location<World>, Location<World>> obj, ConfigurationNode value) throws ObjectMappingException {
        value.getNode("x1").setValue(obj.getLeft().getBlockX());
        value.getNode("y1").setValue(obj.getLeft().getBlockY());
        value.getNode("z1").setValue(obj.getLeft().getBlockZ());
        value.getNode("x2").setValue(obj.getRight().getBlockX());
        value.getNode("y2").setValue(obj.getRight().getBlockY());
        value.getNode("z2").setValue(obj.getRight().getBlockZ());
        value.getNode("worldName").setValue(obj.getLeft().getExtent().getName());
    }
}
