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
package ch.vorburger.minecraft.storeys.web;

import ch.vorburger.minecraft.storeys.simple.Token;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import java.util.Optional;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Implementation of {@link TokenProvider} suitable for testing.
 *
 * @author Michael Vorburger.ch
 */
public class TestTokenProvider implements TokenProvider {

    @Override
    public String getCode(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SecretPublicKeyPair login(String code, String base64PublicKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Token getToken(String tokenAsText) {
        return new Token() {
        };
    }

    @Override
    public Optional<Player> getOptionalPlayer(Token token) {
        return Optional.empty();
    }

    @Override
    public Player getPlayer(Token token) {
        throw new UnsupportedOperationException();
    }

}
