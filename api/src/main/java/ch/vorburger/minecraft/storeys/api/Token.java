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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Token for an operation on the {@link Minecraft} simple API.
 *
 * This identifies either a logged in Player or a "cause".
 */
@DataObject(generateConverter = true)
public class Token {
    // TODO It would be NEAT to make http://Immutables.org support Vert.x @DataObject code generation...

    // only either or one of those is set, never all ("union")
    private String loginCode;    // this is our "old" RSA-ish ch.vorburger.minecraft.storeys.simple.Token
    private String playerSource; // this is our "new" CommandSource as Player cause; from our Map<new-UUID,Player> (not encrypted)

    public Token() {
    }

    public Token(JsonObject jsonObject) {
        TokenConverter.fromJson(jsonObject, this);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        TokenConverter.toJson(this, json);
        return json;
    }

    public String getLoginCode() {
        return loginCode;
    }

    public void setLoginCode(String loginCode) {
        if (playerSource != null) {
            throw new IllegalStateException("playerSource already set");
        }
        this.loginCode = loginCode;
    }

    public String getPlayerSource() {
        return playerSource;
    }

    public void setPlayerSource(String playerSource) {
        if (loginCode != null) {
            throw new IllegalStateException("loginCode already set");
        }
        this.playerSource = playerSource;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (loginCode == null ? 0 : loginCode.hashCode());
        result = prime * result + (playerSource == null ? 0 : playerSource.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Token other = (Token) obj;
        if (loginCode == null) {
            if (other.loginCode != null) {
                return false;
            }
        } else if (!loginCode.equals(other.loginCode)) {
            return false;
        }
        if (playerSource == null) {
            if (other.playerSource != null) {
                return false;
            }
        } else if (!playerSource.equals(other.playerSource)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Token [loginCode=" + loginCode + ", playerSource=" + playerSource + "]";
    }

}
