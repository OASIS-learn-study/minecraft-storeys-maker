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
package ch.vorburger.minecraft.storeys.simple;

import java.util.Optional;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Obtain {@link Token} instances from String.
 *
 * @author Michael Vorburger.ch, based on code by edewit
 */
public interface TokenProvider {

    // TODO Write JavaDoc, and a [JS WD?!] test.. (e.g. unclear where tokenAsText comes from, and why the keys go to/fro client)

    String getCode(Player player);

    SecretPublicKeyPair login(String code, String base64PublicKey);

    // TODO do these methods make any sense, or just rid of Token all together and just "Stringly instead of strongly typed" OK here?

    Token getToken(String tokenAsText);

    Optional<Player> getOptionalPlayer(Token token);

    Player getPlayer(Token token);

    interface SecretPublicKeyPair {
        String getSecret();
        String getBase64PublicKey();
    }

}
