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

import com.google.common.collect.ImmutableSet;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
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

    private static final ImmutableSet<HttpMethod> ALL_HTTP_METHODS = ImmutableSet.<HttpMethod>builder().add(HttpMethod.values()).build();

    protected final int httpPort;
    private HttpServer httpServer;

    protected AbstractHttpServerVerticle(int httpPort) {
        this.httpPort = httpPort;
    }

    @Override
    public final void start(Future<Void> startFuture) throws Exception {
        // http://vertx.io/docs/vertx-web/java/#_cors_handling
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create(/* "scratchx\\.org" */ "*").allowedMethods(ALL_HTTP_METHODS)
            .allowedHeader("Access-Control-Request-Method")
            .allowedHeader("Access-Control-Allow-Credentials")
            .allowedHeader("Access-Control-Allow-Origin")
            .allowedHeader("Access-Control-Allow-Headers")
            .allowedHeader("Content-Type"));

        addRoutes(router);

        httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router::accept).listen(httpPort, asyncResult -> {
            startFuture.handle(asyncResult.mapEmpty());
        });
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
