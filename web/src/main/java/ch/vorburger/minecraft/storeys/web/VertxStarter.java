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
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    private MinecraftVerticle minecraftVerticle;
    private final Queue<String> allVerticleDeploymentIDsToStop = new ConcurrentLinkedQueue<>();

    public CompletionStage<Void> start(int httpPort, ActionsConsumer actionsConsumer) {
        // see https://github.com/eclipse/vert.x/issues/2298 ...
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            System.setProperty("vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
            vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(23));
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }

        minecraftVerticle = new MinecraftVerticle(httpPort, actionsConsumer);
        return deployVerticle(minecraftVerticle)
            .thenRun(() -> LOG.info("Started Vert.x distributed BiDi event-bus HTTP server on port {}", httpPort));
    }

    public CompletionStage<Void> deployVerticle(Verticle newVerticle) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        vertx.deployVerticle(newVerticle, new DeploymentOptions(), (Handler<AsyncResult<String>>) result -> {
            if (result.succeeded()) {
                allVerticleDeploymentIDsToStop.add(result.result());
                future.complete(null);
            } else {
                future.completeExceptionally(result.cause());
            }
        });
        return future;
    }

    public void stop() throws Exception {
        for (String deploymentID : allVerticleDeploymentIDsToStop) {
            vertx.undeploy(deploymentID);
        }
        if (vertx != null) {
            vertx.close();
        }
    }

    @Override
    public void send(Object message) {
        LOG.info("Sending message: {}", message);
        minecraftVerticle.send(message);
    }

    // This main() is only for quick local testing; the Minecraft Sponge plugin directly uses above and not this
    public static void main(String[] args) throws Exception {
        VertxStarter starter = new VertxStarter();
        starter.start(8080, new ActionsConsumer(null, null, null, null, null)).toCompletableFuture().get();

        System.out.println("Running now... press Enter to Stop.");
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in, defaultCharset()));
        buffer.readLine();

        starter.stop();
    }

}
