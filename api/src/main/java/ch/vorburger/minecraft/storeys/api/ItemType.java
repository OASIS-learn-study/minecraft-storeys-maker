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
package ch.vorburger.minecraft.storeys.api;

import static java.util.Objects.requireNonNull;
import static org.spongepowered.api.item.ItemTypes.APPLE;
import static org.spongepowered.api.item.ItemTypes.BEEF;
import static org.spongepowered.api.item.ItemTypes.BEETROOT;
import static org.spongepowered.api.item.ItemTypes.BOAT;
import static org.spongepowered.api.item.ItemTypes.BOOK;
import static org.spongepowered.api.item.ItemTypes.BOW;
import static org.spongepowered.api.item.ItemTypes.BOWL;
import static org.spongepowered.api.item.ItemTypes.BREAD;
import static org.spongepowered.api.item.ItemTypes.CACTUS;
import static org.spongepowered.api.item.ItemTypes.CAKE;
import static org.spongepowered.api.item.ItemTypes.CARROT;
import static org.spongepowered.api.item.ItemTypes.CAULDRON;
import static org.spongepowered.api.item.ItemTypes.CHICKEN;
import static org.spongepowered.api.item.ItemTypes.CLOCK;
import static org.spongepowered.api.item.ItemTypes.COOKIE;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Optional;

public enum ItemType implements SpongeCataloged<Optional<org.spongepowered.api.item.ItemType>> {

    /**
     * Not holding anything in hand.
     */
    Nothing,

    Apple(APPLE), Beef(BEEF), Beetroot(BEETROOT), Boat(BOAT), Book(BOOK), Bow(BOW), Bowl(BOWL), Bread(BREAD), Cactus(CACTUS), Cake(CAKE),
    Carrot(CARROT), Cauldron(CAULDRON), Chicken(CHICKEN), Clock(CLOCK), Cookie(COOKIE),

    /**
     * Holding none of the known previously listed items in hand.
     */
    Unknown;

    private final Optional<org.spongepowered.api.item.ItemType> itemType;
    private static Map<org.spongepowered.api.item.ItemType, ItemType> INVERSE;

    ItemType(org.spongepowered.api.item.ItemType itemType) {
        this.itemType = Optional.of(itemType);
    }

    ItemType() {
        this.itemType = Optional.empty();
    }

    @Override public Optional<org.spongepowered.api.item.ItemType> getCatalogType() {
        return itemType;
    }

    public static Optional<ItemType> getOptionalEnum(org.spongepowered.api.item.ItemType catalogType) {
        if (INVERSE == null) {
            Builder<org.spongepowered.api.item.ItemType, ItemType> builder = ImmutableMap.<org.spongepowered.api.item.ItemType, ItemType>builder();
            for (ItemType itemType : ItemType.values()) {
                itemType.getCatalogType().ifPresent(newCatalogType -> builder.put(newCatalogType, itemType));
            }
            INVERSE = builder.build();
        }
        return Optional.ofNullable(INVERSE.get(requireNonNull(catalogType, "catalogType")));
    }

    public static ItemType getEnum(org.spongepowered.api.item.ItemType catalogType) {
        return getOptionalEnum(catalogType).orElse(Unknown);
    }
}
