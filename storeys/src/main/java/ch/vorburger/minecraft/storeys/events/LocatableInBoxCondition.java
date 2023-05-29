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
package ch.vorburger.minecraft.storeys.events;

import static java.lang.Double.parseDouble;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Splitter;
import java.util.Iterator;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

public class LocatableInBoxCondition implements Condition {

    private static final Splitter SLASH_SPLITTER = Splitter.on(' ');

    private Player effectedPlayer;

    private final ServerWorld world;
    private final double minX, maxX, minY, maxY, minZ, maxZ;

    public LocatableInBoxCondition(ServerWorld world, ServerLocation boxCorner1, ServerLocation boxCorner2) {
        this.world = requireNonNull(world, "world");
        if (!boxCorner1.inWorld(boxCorner2.world())) {
            throw new IllegalArgumentException("boxCorner1 & boxCorner2 are not in the same World Extent");
        }
        if (!boxCorner1.inWorld(this.world)) {
            throw new IllegalArgumentException("boxCorner is not in the same World Extent as Locatable (Player)");
        }
        minX = min(boxCorner1.x(), boxCorner2.x());
        maxX = max(boxCorner1.x(), boxCorner2.x());
        minY = min(boxCorner1.y(), boxCorner2.y());
        maxY = max(boxCorner1.y(), boxCorner2.y());
        minZ = min(boxCorner1.z(), boxCorner2.z());
        maxZ = max(boxCorner1.z(), boxCorner2.z());
    }

    public LocatableInBoxCondition(ServerWorld world, String coordinates) {
        this(getCornerLocations(world, coordinates));
    }

    public LocatableInBoxCondition(Pair<ServerLocation, ServerLocation> corners) {
        this(corners.getLeft().world(), corners.getLeft(), corners.getRight());
    }

    private static Pair<ServerLocation, ServerLocation> getCornerLocations(ServerWorld world, String coordinates) {
        Iterator<String> ints = SLASH_SPLITTER.split(coordinates).iterator();
        ServerLocation cornerA = ServerLocation.of(world, parseDouble(ints.next()), parseDouble(ints.next()), parseDouble(ints.next()));
        ServerLocation cornerB = ServerLocation.of(world, parseDouble(ints.next()), parseDouble(ints.next()), parseDouble(ints.next()));
        return Pair.of(cornerA, cornerB);
    }

    @Override public boolean isHot() {
        for (Player player : this.world.players()) {
            final ServerLocation location = player.serverLocation();
            if (location.inWorld(world)) {
                double x = location.x();
                double y = location.y();
                double z = location.z();
                boolean hit = x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
                if (hit) {
                    this.effectedPlayer = player;
                }
                return hit;
            }
        }
        return false;
    }

    @Override public Player getEffectedPlayer() {
        return effectedPlayer;
    }
}
