/**
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

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Splitter;
import java.util.Iterator;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class LocatableInBoxCondition implements Condition {

    private static final Splitter SLASH_SPLITTER = Splitter.on('/');

    private final Locatable locatable;

    private final World world;
    private final int minX, maxX, minY, maxY, minZ, maxZ;

    public LocatableInBoxCondition(Locatable locatable, Location<World> boxCorner1, Location<World> boxCorner2) {
        this.locatable  = requireNonNull(locatable,  "locatable");
        if (!boxCorner1.inExtent(boxCorner2.getExtent())) {
            throw new IllegalArgumentException("boxCorner1 & boxCorner2 are not in the same World Extent");
        }
        this.world = locatable.getWorld();
        if (!boxCorner1.inExtent(this.world)) {
            throw new IllegalArgumentException("boxCorner is not in the same World Extent as Locatable (Player)");
        }
        minX = min(boxCorner1.getBlockX(), boxCorner2.getBlockX());
        maxX = max(boxCorner1.getBlockX(), boxCorner2.getBlockX());
        minY = min(boxCorner1.getBlockY(), boxCorner2.getBlockY());
        maxY = max(boxCorner1.getBlockY(), boxCorner2.getBlockY());
        minZ = min(boxCorner1.getBlockZ(), boxCorner2.getBlockZ());
        maxZ = max(boxCorner1.getBlockZ(), boxCorner2.getBlockZ());
    }

    public LocatableInBoxCondition(Locatable locatable, String coordinates) {
        this(locatable, getCornerLocations(locatable, coordinates));
    }

    private LocatableInBoxCondition(Locatable locatable, Pair<Location<World>, Location<World>> corners) {
        this(locatable, corners.getLeft(), corners.getRight());
    }

    private static Pair<Location<World>, Location<World>> getCornerLocations(Locatable locatable, String coordinates) {
        Iterator<String> ints = SLASH_SPLITTER.split(coordinates).iterator();
        Location<World> cornerA = new Location<>(locatable.getWorld(), parseInt(ints.next()), parseInt(ints.next()), parseInt(ints.next()));
        Location<World> cornerB = new Location<>(locatable.getWorld(), parseInt(ints.next()), parseInt(ints.next()), parseInt(ints.next()));
        return Pair.of(cornerA, cornerB);
    }

    @Override
    public boolean isHot() {
        final Location<World> location = locatable.getLocation();
        if (location.inExtent(world)) {
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            return x >= minX && x <= maxX
                && y >= minY && x <= maxY
                && z >= minZ && z <= maxZ;
        }
        return false;
    }

}
