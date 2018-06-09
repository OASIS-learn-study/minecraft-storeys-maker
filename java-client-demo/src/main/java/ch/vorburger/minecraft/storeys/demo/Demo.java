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
package ch.vorburger.minecraft.storeys.demo;

import ch.vorburger.minecraft.storeys.api.Minecraft;
import ch.vorburger.minecraft.storeys.api.Token;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo how to remote connects to a Minecraft server, via Vert.x, and runs actions via our remoting API.
 *
 * @author Michael Vorburger.ch
 */
public class Demo {

    private static final Logger LOG = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Minecraft minecraft = Minecraft.createProxy(vertx);
        String jsonToken = "{\"playerSource\":\"c24d5720-8f8a-423f-b20f-adf4d53859ee\"}";
        Token token = new Token(new JsonObject(jsonToken));
        minecraft.showTitle(token , "hello, world!", event -> {
		    if (!event.succeeded()) {
		        LOG.error("showTitle() failed", event.cause());
		    }
		});
    }

}
