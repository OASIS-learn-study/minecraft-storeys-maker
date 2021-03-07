/*
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

import com.google.common.collect.ImmutableSet;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;

/**
 * Vert.x Verticle with web server.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractHttpServerVerticle extends AbstractVerticle {

    private static final ImmutableSet<HttpMethod> ALL_HTTP_METHODS = ImmutableSet.<HttpMethod>builder().addAll(HttpMethod.values()).build();

    protected final int httpPort;
    private HttpServer httpServer;

    protected AbstractHttpServerVerticle(int httpPort) {
        this.httpPort = httpPort;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        start();

        // http://vertx.io/docs/vertx-web/java/#_cors_handling
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create().allowedMethods(ALL_HTTP_METHODS)
                .allowCredentials(true)
                .allowedHeader("Access-Control-Allow-Method")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Access-Control-Allow-Credentials")
                .allowedHeader("Content-Type"));

        addRoutes(router);

        httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router).listen(httpPort, asyncResult -> {
            startPromise.handle(asyncResult.mapEmpty());
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
    }

    @Override
    public final void stop() throws Exception {
        stopVerticle();
        httpServer.close();
    }

    protected void stopVerticle() throws Exception {
    }

    abstract protected void addRoutes(Router router);
}
