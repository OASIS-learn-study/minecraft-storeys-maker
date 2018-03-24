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

import org.spongepowered.api.entity.living.player.Player;

/**
 * Obtain {@link Token} instances from String.
 *
 * @author Michael Vorburger.ch, based on code by edewit
 */
public interface TokenProvider {

    Token getToken(String tokenAsText);

    SecretPublicKeyPair login(String code, String base64PublicKey);

	String getCode(Player player);

	interface SecretPublicKeyPair {
        String getSecret();
        String getBase64PublicKey();
    }
}
