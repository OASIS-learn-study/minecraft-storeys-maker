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
package ch.vorburger.minecraft.storeys.simple.impl;

import ch.vorburger.minecraft.storeys.simple.Token;
import java.util.Optional;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Implementation of {@link Token} holding Minecraft Player object.
 *
 * @author Michael Vorburger.ch
 */
// TODO /* intentionally package local */
public class PlayerTokenImpl implements Token {

    private final Optional<Player> optPlayer;

    PlayerTokenImpl(Optional<Player> optPlayer) {
        this.optPlayer = optPlayer;
    }

    public Optional<Player> getOptionalPlayer() {
        return optPlayer;
    }

}
