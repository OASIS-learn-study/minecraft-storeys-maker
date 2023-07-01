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
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

@SuppressWarnings("serial")
public class LocationPairSerializer implements TypeSerializer<Pair<ServerLocation, ServerLocation>> {
    public static TypeToken<Pair<ServerLocation, ServerLocation>> TYPE = new TypeToken<Pair<ServerLocation, ServerLocation>>(){};

    @Override public Pair<ServerLocation, ServerLocation> deserialize(Type type, ConfigurationNode value)
            throws SerializationException {
        final String worldName = value.node("worldName").getString();
        final double x1 = value.node("x1").getDouble();
        final double y1 = value.node("y1").getDouble();
        final double z1 = value.node("z1").getDouble();

        final double x2 = value.node("x2").getDouble();
        final double y2 = value.node("y2").getDouble();
        final double z2 = value.node("z2").getDouble();

        final List<ServerWorld> worlds = Sponge.server().worldManager().worlds().stream()
                .filter(serverWorld -> serverWorld.world().key().value().equals(worldName)).collect(Collectors.toList());
        ServerWorld world = worlds.get(0);
        if (world == null) {
            throw new RuntimeException("world not initialized?");
        }
        final ServerLocation point1 = ServerLocation.of(world, x1, y1, z1);
        final ServerLocation point2 = ServerLocation.of(world, x2, y2, z2);
        return Pair.of(point1, point2);
    }

    @Override public void serialize(Type type, Pair<ServerLocation, ServerLocation> obj, ConfigurationNode value)
            throws SerializationException {
        value.node("x1").set(obj.getLeft().x());
        value.node("y1").set(obj.getLeft().y());
        value.node("z1").set(obj.getLeft().z());
        value.node("x2").set(obj.getRight().x());
        value.node("y2").set(obj.getRight().y());
        value.node("z2").set(obj.getRight().z());
        value.node("worldName").set(obj.getLeft().worldKey().value());
    }
}
