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

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Launcher for Vert.x.
 *
 * @author Michael Vorburger.ch
 */
public class VertxStarter {

    private final Vertx vertx;

    public VertxStarter(Vertx vertx) {
        // see https://github.com/eclipse/vert.x/issues/2298 ...
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            System.setProperty("vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
            this.vertx = vertx;
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    public VertxStarter() {
        this(Vertx.vertx(new VertxOptions().setWorkerPoolSize(23)));
    }

    public CompletionStage<Void> deployVerticle(Verticle newVerticle) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        vertx.deployVerticle(newVerticle, new DeploymentOptions(), (Handler<AsyncResult<String>>) result -> {
            if (result.succeeded()) {
                future.complete(null);
            } else {
                future.completeExceptionally(result.cause());
            }
        });
        return future;
    }

    public void stop() throws Exception {
        if (vertx != null) {
            vertx.close();
        }
    }

    public Vertx vertx() {
        return vertx;
    }

}
