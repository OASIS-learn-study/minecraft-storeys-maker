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
package ch.vorburger.minecraft.storeys.api.impl;

import static java.util.Objects.requireNonNull;

import ch.vorburger.minecraft.storeys.api.Token;
import ch.vorburger.minecraft.storeys.simple.impl.NotLoggedInException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Optional;
import java.util.UUID;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Create {@link Token} instances and turn then back into {@link Player}s.
 *
 * @author Michael Vorburger.ch
 */
public class TokenProvider {

    private final BiMap<UUID, Player> playerTokens = HashBiMap.create();

    public Token getToken(Player player) {
        UUID uuid = playerTokens.inverse().computeIfAbsent(player, p -> UUID.randomUUID());
        Token token = new Token();
        token.setPlayerSource(uuid.toString());
        return token;
    }

    public Optional<Player> getOptionalPlayer(Token token) {
        return Optional.ofNullable(playerTokens.get(tokenToUUID(token)));
    }

    public Player getPlayer(Token token) {
        return getOptionalPlayer(token).orElseThrow(() -> new NotLoggedInException(token.getPlayerSource()));
    }

    public void invalidate(Player player) {
        playerTokens.inverse().remove(player);
    }

    public void invalidate(Token token) {
        playerTokens.remove(tokenToUUID(token));
    }

    private UUID tokenToUUID(Token token) {
        return UUID.fromString(requireNonNull(token, "token").getPlayerSource());
    }

}
