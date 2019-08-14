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
package study.learn.storeys.engine.prompters;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import study.learn.storeys.engine.Interactlet;

@ClientEndpoint
@ServerEndpoint(value = "/prompt/")
public class WebSocketServer {
    private Map<String, SimplePrompter<?>> sessions = new ConcurrentHashMap<>();

    private Interactlet initialInteractlet;

    private WebSocketServer() {}

    public static WebSocketServer newInstance(Interactlet interactlet) {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        WebSocketServer webSocketServer = new WebSocketServer();
        webSocketServer.initialInteractlet = interactlet;
        try {
            ServerContainer container = WebSocketServerContainerInitializer.configureContext(context);
            ServerEndpointConfig config = ServerEndpointConfig.Builder.create(webSocketServer.getClass(),
                                                                              webSocketServer.getClass().getAnnotation(ServerEndpoint.class).value())
                    .configurator(new ServerEndpointConfig.Configurator() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                            return (T) webSocketServer;
                        }
                    })
                    .build();
            container.addEndpoint(config);

            server.start();
        } catch (Throwable t) {
            throw new RuntimeException("could not start embedded jetty", t);
        }
        return webSocketServer;
    }

    @OnOpen
    public void onOpen(Session session) {
        SimplePrompter<Void> prompter = new SimplePrompter<>(new WebsocketPrompterIO(session));
        sessions.put(session.getId(), prompter);
        CompletableFuture.runAsync(() -> {
            try {
                initialInteractlet.interact(prompter);
            } catch (IOException e) {
                throw new RuntimeException("could not write to websocket", e);
            }
        });
    }

    @OnMessage
    public void handleTextMessage(Session session, String message) {
        ((WebsocketPrompterIO)sessions.get(session.getId()).getIo()).trigger(message);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());
    }
}
