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
package ch.vorburger.minecraft.storeys.simple.impl;

import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Implementation of {@link TokenProvider} API.
 *
 * @author edewit - original author (in ActionsConsumer and LoginCommand)
 * @author Michael Vorburger.ch - refactored out into here, and use ConcurrentHashMap
 */
@Singleton
public class TokenProviderImpl implements TokenProvider {
    private String adminLoginCode = "learn.study.m1n3craft";

    private final Map<String, Token> validLogins = new ConcurrentHashMap<>();
    private long tokenValidTime;

    public TokenProviderImpl() {
        this(3, TimeUnit.MINUTES);
    }

    TokenProviderImpl(int pollInterval, TimeUnit unit) {
        this(pollInterval, unit, TimeUnit.MINUTES.toMillis(15));
    }

    TokenProviderImpl(int pollInterval, TimeUnit pollIntevalUnit, long tokenValidTime) {
        this.tokenValidTime = tokenValidTime;
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> validLogins.values().removeIf(token -> !token.isValid()), 0, pollInterval, pollIntevalUnit);
        adminLoginCode = getSystemPropertyEnvVarOrDefault("storeys_admincode", adminLoginCode);
    }

    private String getSystemPropertyEnvVarOrDefault(String propertyName, String defaultValue) {
        String property = System.getProperty(propertyName);
        if (property != null) {
            return property;
        }
        property = System.getenv(propertyName);
        if (property != null) {
            return property;
        }
        return defaultValue;
    }

    @Override
    public String getCode(Player player) {
        String code = UUID.randomUUID().toString();
        validLogins.put(code, new Token(player.getIdentifier(), tokenValidTime));
        return code;
    }

    @Override
    public String login(String code) {
        if (adminLoginCode.equals(code)) {
            return "";
        }
        final Token token = validLogins.remove(code);
        String playerUUID = token != null ? token.token : null;
        if (playerUUID == null) {
            throw new NotLoggedInException(code);
        }
        return playerUUID;
    }

    private class Token {
        private long time = System.currentTimeMillis();
        private long timeToLive;
        private String token;

        Token(String token, long timeToLive) {
            this.token = token;
            this.timeToLive = timeToLive;
        }

        boolean isValid() {
            return System.currentTimeMillis() - time < timeToLive;
        }
    }
}
