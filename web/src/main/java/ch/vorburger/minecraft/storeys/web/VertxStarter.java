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

import static java.nio.charset.Charset.defaultCharset;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher for Vert.x.
 *
 * @author Michael Vorburger.ch
 */
public class VertxStarter implements EventBusSender {

    private static final Logger LOG = LoggerFactory.getLogger(VertxStarter.class);

    private Vertx vertx;
    private MinecraftVerticle verticle;

    public java.util.concurrent.Future<Void> start(int httpPort, ActionsConsumer actionsConsumer) {
        System.setProperty("vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
        vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(23));

        verticle = new MinecraftVerticle(httpPort, actionsConsumer);

        CompletableFuture<Void> future = new CompletableFuture<>();
        vertx.deployVerticle(verticle, new DeploymentOptions(), (Handler<AsyncResult<String>>) result -> {
            if (result.succeeded()) {
                future.complete(null);
                LOG.info("Started Vert.x distributed BiDi event-bus HTTP server on port {}", httpPort);
            } else {
                future.completeExceptionally(result.cause());
            }
        });
        return future;
    }

    public void stop() {
        if (vertx != null) {
            vertx.close();
        }
    }

    @Override
    public void send(Object message) {
        verticle.send(message);
    }

    // This main() is only for quick local testing; the Minecraft Sponge plugin directly uses above and not this
    public static void main(String[] args) throws Exception {
        VertxStarter starter = new VertxStarter();
        starter.start(8080, new ActionsConsumer(null, null, null, null)).get();

        System.out.println("Running now... press Enter to Stop.");
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in, defaultCharset()));
        buffer.readLine();

        starter.stop();
    }

}
