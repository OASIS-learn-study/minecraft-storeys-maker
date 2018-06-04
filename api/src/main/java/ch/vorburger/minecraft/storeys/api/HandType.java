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
package ch.vorburger.minecraft.storeys.api;

import static org.spongepowered.api.data.type.HandTypes.MAIN_HAND;
import static org.spongepowered.api.data.type.HandTypes.OFF_HAND;

public enum HandType implements SpongeCataloged<org.spongepowered.api.data.type.HandType> {

    MainHand(MAIN_HAND), OffHand(OFF_HAND);


    private final org.spongepowered.api.data.type.HandType handType;

    HandType(org.spongepowered.api.data.type.HandType handType) {
        this.handType = handType;
    }

    @Override
    public org.spongepowered.api.data.type.HandType getCatalogType() {
        return handType;
    }

}
