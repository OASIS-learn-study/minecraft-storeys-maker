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

import static java.util.Objects.requireNonNull;

import ch.vorburger.minecraft.storeys.api.Minecraft;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Handler<Message<JsonObject>> actionsConsumer;

    public MinecraftVerticle(int httpPort, Minecraft minecraft) {
        super(httpPort);
        this.minecraft = minecraft;
    }

    public void setActionsConsumer(Handler<Message<JsonObject>> actionsConsumer) {
        this.actionsConsumer = actionsConsumer;
    }

    @Override
    public void start() throws Exception {
        String address = Minecraft.ADDRESS;
        new ServiceBinder(vertx).setAddress(address).register(Minecraft.class, minecraft);
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
        sockJSHandler.bridge(bridgeOptions);
        router.route("/eventbus/*").handler(sockJSHandler);
    }

    @Override
    public void send(Object message) {
        vertx.eventBus().publish(EVENTBUS_MINECRAFT_EVENTS_ADDRESS, message);
        LOG.info("Sent to EventBus: {}", message);
    }
}
