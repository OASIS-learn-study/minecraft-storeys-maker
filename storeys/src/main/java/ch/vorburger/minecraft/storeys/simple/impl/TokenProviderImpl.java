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
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Implementation of {@link TokenProvider} API.
 *
 * @deprecated to be phased out and replaced by ch.vorburger.minecraft.storeys.api.impl.TokenProvider
 *
 * @author edewit - original author (in ActionsConsumer and LoginCommand)
 * @author Michael Vorburger.ch - refactored out into here, and use ConcurrentHashMap
 */
@Deprecated
public class TokenProviderImpl implements TokenProvider {

    private final Game game;
    private final RSAUtil rsaUtil = new RSAUtil();

    // TODO would be better to have something to invalidate entries that are old?
    private final Map<String, String> validLogins = new ConcurrentHashMap<>();
    private final Map<String, Player> activeSessions = new ConcurrentHashMap<>();

    public TokenProviderImpl(Game game) {
        this.game = game;
    }

    @Override
    public String getCode(Player player) {
        String code = UUID.randomUUID().toString();
        validLogins.put(code, player.getIdentifier());
        return code;
    }

    @Override
    public SecretPublicKeyPair login(String code, String base64PublicKey) {
        String playerUUID = validLogins.remove(code);
        if (playerUUID == null) {
            throw new NotLoggedInException("playerUUID == null");
        }
        Optional<Player> optPlayer = game != null ? game.getServer().getPlayer(UUID.fromString(playerUUID)) : Optional.empty();
        String secret = UUID.randomUUID().toString();

        String encrypted = rsaUtil.encrypt(secret, base64PublicKey);
        activeSessions.put(secret, optPlayer.orElse(null));

        return new TokenProvider.SecretPublicKeyPair() {

            @Override
            public String getSecret() {
                return encrypted;
            }

            @Override
            public String getBase64PublicKey() {
                return rsaUtil.getBase64PublicKey();
            }

        };
    }

    @Override
    public Token getToken(String tokenAsText) {
        return new PlayerTokenImpl(tokenAsText != null ? Optional.ofNullable(activeSessions.get(rsaUtil.decrypt(tokenAsText))) : Optional.empty(), tokenAsText);
    }

    @Override
    public Optional<Player> getOptionalPlayer(Token token) {
        return ((PlayerTokenImpl) token).optPlayer;
    }

    @Override
    public Player getPlayer(Token token) throws NotLoggedInException {
        return getOptionalPlayer(Objects.requireNonNull(token, "token")).orElseThrow(() -> new NotLoggedInException(token));
    }

    private static class PlayerTokenImpl extends Token {

        final Optional<Player> optPlayer;
        final String tokenAsText;

        PlayerTokenImpl(Optional<Player> optPlayer, String tokenAsText) {
            this.optPlayer = optPlayer;
            this.tokenAsText = tokenAsText;
        }

        @Override
        public String toString() {
            return "Token{text=" + tokenAsText + "}, player={" + optPlayer + "}";
        }

    }
}
