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

import com.google.common.collect.ImmutableSet;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vert.x Verticle for Minecraft Storeys web API, usable e.g. by ScratchX extension.
 *
 * @author Michael Vorburger.ch
 */
public class MinecraftVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(MinecraftVerticle.class);

    private static final String EVENTBUS_MINECRAFT_ACTIONS_ADDRESS = "mcs.actions";
    private static final String EVENTBUS_MINECRAFT_EVENTS_ADDRESS = "mcs.events";

    private static final Set<HttpMethod> ALL_HTTP_METHODS = ImmutableSet.<HttpMethod>builder().add(HttpMethod.values()).build();

    private final int httpPort;
    private final ActionsConsumer actionsConsumer;

    private HttpServer httpServer;

    public MinecraftVerticle(int httpPort, ActionsConsumer actionsConsumer) {
        this.httpPort = httpPort;
        this.actionsConsumer = actionsConsumer;
    }

    @Override
    public void start() throws Exception {
        // http://vertx.io/docs/vertx-web/java/#_cors_handling
        Router router = Router.router(vertx);
        router.route().handler(CorsHandler.create(/* "scratchx\\.org" */ "*").allowedMethods(ALL_HTTP_METHODS)
            .allowedHeader("Access-Control-Request-Method")
            .allowedHeader("Access-Control-Allow-Credentials")
            .allowedHeader("Access-Control-Allow-Origin")
            .allowedHeader("Access-Control-Allow-Headers")
            .allowedHeader("Content-Type"));

        vertx.eventBus().consumer(EVENTBUS_MINECRAFT_ACTIONS_ADDRESS, actionsConsumer);

        SockJSHandlerOptions sockJSHandleOptions = new SockJSHandlerOptions().setHeartbeatInterval(5432);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockJSHandleOptions);
        PermittedOptions inboundPermitted1 = new PermittedOptions().setAddress(EVENTBUS_MINECRAFT_ACTIONS_ADDRESS);
        PermittedOptions outboundPermitted1 = new PermittedOptions().setAddress(EVENTBUS_MINECRAFT_EVENTS_ADDRESS);
        BridgeOptions bridgeOptions = new BridgeOptions().addInboundPermitted(inboundPermitted1).addOutboundPermitted(outboundPermitted1);
        sockJSHandler.bridge(bridgeOptions);
        router.route("/eventbus/*").handler(sockJSHandler);

        httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router::accept).listen(httpPort, asyncResult -> {
            if (asyncResult.succeeded()) {
                LOG.info("HTTP server started on port {}", httpPort);
            } else {
                LOG.error("Failed to start HTTP server on port {}", httpPort, asyncResult.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        httpServer.close();
    }

}
