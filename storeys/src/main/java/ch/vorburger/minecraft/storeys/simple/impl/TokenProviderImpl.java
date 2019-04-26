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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Implementation of {@link TokenProvider} API.
 *
 * @author edewit - original author (in ActionsConsumer and LoginCommand)
 * @author Michael Vorburger.ch - refactored out into here, and use ConcurrentHashMap
 */
public class TokenProviderImpl implements TokenProvider {

private final Game game;
    private final Map<String, Token> validLogins = new ConcurrentHashMap<>();

    public TokenProviderImpl(Game game) {
        this(game, 1, TimeUnit.SECONDS);
    }

    public TokenProviderImpl(Game game, int timeout, TimeUnit unit) {
        this.game = game;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> validLogins.values().removeIf(token -> !token.isValid()), 0, timeout, unit);
    }

    @Override
    public String getCode(Player player) {
        String code = UUID.randomUUID().toString();
        validLogins.put(code, new Token(player.getIdentifier()));
        return code;
    }

    @Override
    public String login(String code) {
        final Token token = validLogins.remove(code);
        String playerUUID = token != null ? token.token : null;
        if (playerUUID == null) {
            throw new NotLoggedInException(code);
        }
        return playerUUID;
    }

    @Override
    public Player getPlayer(String playerUUID) throws NotLoggedInException {
        return game.getServer().getPlayer(UUID.fromString(playerUUID)).orElseThrow(() -> new NotLoggedInException(playerUUID));
    }

    private class Token {
        private long time = System.currentTimeMillis();
        private String token;

        Token(String token) {
            this.token = token;
        }

        String getToken() {
            return token;
        }

        boolean isValid() {
            return System.currentTimeMillis() - time < 15 * 60 * 1000;
        }
    }
}
