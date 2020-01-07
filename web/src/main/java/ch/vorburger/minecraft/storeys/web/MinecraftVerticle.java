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

import ch.vorburger.minecraft.storeys.api.Minecraft;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.simple.impl.NotLoggedInException;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import static java.util.Objects.requireNonNull;

/**
 * Vert.x Verticle for Minecraft Storeys web API, usable e.g. by ScratchX extension.
 *
 * @author Michael Vorburger.ch
 */
public class MinecraftVerticle extends AbstractHttpServerVerticle implements EventBusSender {

    private static final Logger LOG = LoggerFactory.getLogger(MinecraftVerticle.class);

    private static final String EVENTBUS_MINECRAFT_ACTIONS_ADDRESS = "mcs.actions";
    private static final String EVENTBUS_MINECRAFT_EVENTS_ADDRESS = "mcs.events";

    private final Minecraft minecraft;
    private final TokenProvider tokenProvider;
    private Handler<Message<JsonObject>> actionsConsumer;

    @Inject
    public MinecraftVerticle(@Named("http-port") int httpPort, Minecraft minecraft, TokenProvider tokenProvider) {
        super(httpPort);
        this.minecraft = minecraft;
        this.tokenProvider = tokenProvider;
    }

    @Inject
    public void setActionsConsumer(Handler<Message<JsonObject>> actionsConsumer) {
        this.actionsConsumer = actionsConsumer;
    }

    @Override
    public void start() throws Exception {
        String address = Minecraft.ADDRESS;
        new ServiceBinder(vertx).setAddress(address).addInterceptor(new LoggingInterceptor()).register(Minecraft.class, minecraft);
        LOG.info("Registered service on the event bus at address: {}", address);
    }

    @Override
    public void stopVerticle() throws Exception {
    }

    @Override
    protected void addRoutes(Router router) {
        vertx.eventBus().consumer(EVENTBUS_MINECRAFT_ACTIONS_ADDRESS, requireNonNull(actionsConsumer, "missing setActionsConsumer()"));

        SockJSHandlerOptions sockJSHandleOptions = new SockJSHandlerOptions().setHeartbeatInterval(5432);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockJSHandleOptions);
        PermittedOptions inboundPermitted1 = new PermittedOptions().setAddress(Minecraft.ADDRESS);
        PermittedOptions inboundPermitted2 = new PermittedOptions().setAddress(EVENTBUS_MINECRAFT_ACTIONS_ADDRESS);
        PermittedOptions outboundPermitted1 = new PermittedOptions().setAddress(EVENTBUS_MINECRAFT_EVENTS_ADDRESS);
        BridgeOptions bridgeOptions = new BridgeOptions().addInboundPermitted(inboundPermitted1)
                .addInboundPermitted(inboundPermitted2).addOutboundPermitted(outboundPermitted1);

        JWTAuthOptions authConfig = new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()
                        .setType("jceks")
                        .setPath("keystore.jceks")
                        .setPassword("_2y47[-53YLf}/frv.Q\""));

        JWTAuth authProvider = JWTAuth.create(vertx, authConfig);

        router.route("/login/:code").handler(ctx -> {
            try {
                String playerUUID = tokenProvider.login(ctx.request().getParam("code"));
                ctx.response().end(authProvider.generateToken(new JsonObject().put("playerUUID", playerUUID), new JWTOptions()));
            } catch (NotLoggedInException e) {
                ctx.fail(401);
            }
        });

        sockJSHandler.bridge(bridgeOptions);
        router.route("/eventbus/*").handler(ctx -> {
            String token = ctx.request().getParam("token");
            authProvider.authenticate(new JsonObject().put("jwt", token), (result) -> {
                if (result.succeeded()) {
                    sockJSHandler.handle(ctx);
                } else {
                    ctx.fail(401);
                }
            });
        });

        LOG.info("Started Vert.x distributed BiDi event-bus HTTP server on port {}", httpPort);
    }

    @Override
    public void send(Object message) {
        vertx.eventBus().publish(EVENTBUS_MINECRAFT_EVENTS_ADDRESS, message);
        LOG.info("Sent to EventBus: {}", message);
    }
}
