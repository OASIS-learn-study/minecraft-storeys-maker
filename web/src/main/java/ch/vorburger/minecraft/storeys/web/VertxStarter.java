/**
 * ch.vorburger.minecraft.storeys
 *
 * Copyright (C) 2016 - 2017 Michael Vorburger.ch <mike@vorburger.ch>
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

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher for Vert.x.
 *
 * @author Michael Vorburger.ch
 */
public class VertxStarter {

    private static final Logger LOG = LoggerFactory.getLogger(VertxStarter.class);

    private Vertx vertx;

    // TODO Could return Future so that caller knows if start up worked..
    public void start(int httpPort, ActionsConsumer actionsConsumer) {
        System.setProperty("vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
        vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(23));

        vertx.deployVerticle(new MinecraftVerticle(httpPort, actionsConsumer), new DeploymentOptions(), (Handler<AsyncResult<String>>) event -> {
            if (event.failed()) {
                LOG.error("Failed to start Verticle", event.cause());
            } else {
                LOG.info("Verticle started successfully");
            }
        });
    }

    public void stop() {
        if (vertx != null) {
            vertx.close();
        }
    }

    // This main() is only for quick local testing; the Minecraft Sponge plugin directly uses above and not this
    public static void main(String[] args) throws IOException {
        VertxStarter starter = new VertxStarter();
        starter.start(8080, new ActionsConsumer(null));

        System.out.println("Running now... press Enter to Stop.");
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        buffer.readLine();

        starter.stop();
    }

}
