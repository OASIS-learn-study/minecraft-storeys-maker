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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import study.learn.storeys.engine.Text;

@ClientEndpoint
@ServerEndpoint(value = "/prompt/")
public class WebSocketPrompterIO implements SimplePrompterIO {
    private Map<String, Session> sessions = new ConcurrentHashMap<>();

    private Map<String, String> messages = new ConcurrentHashMap<>();

    private static CountDownLatch latch = new CountDownLatch(1);

    private static CountDownLatch latch2 = new CountDownLatch(1);

    private WebSocketPrompterIO() {}

    public static WebSocketPrompterIO newInstance() {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        WebSocketPrompterIO webSocketPrompterIO = new WebSocketPrompterIO();
        try {
            ServerContainer container = WebSocketServerContainerInitializer.configureContext(context);
            ServerEndpointConfig config = ServerEndpointConfig.Builder.create(webSocketPrompterIO.getClass(),
                                                                              webSocketPrompterIO.getClass().getAnnotation(ServerEndpoint.class).value())
                    .configurator(new ServerEndpointConfig.Configurator() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                            return (T) webSocketPrompterIO;
                        }
                    })
                    .build();
            container.addEndpoint(config);

            server.start();
        } catch (Throwable t) {
            throw new RuntimeException("could not start embedded jetty", t);
        }
        return webSocketPrompterIO;
    }

    @Override
    public String readLine(String prompt, List<Text> choices) throws IOException {
        try {
            latch2.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        RemoteEndpoint.Async remote = sessions.values().iterator().next().getAsyncRemote();
        for (int i = 0; i < choices.size(); i++) {
            remote.sendText("    " + (i + 1) + ": " + choices.get(i).getString());
        }
        remote.sendText(prompt);
        return waitForResponse();
    }

    private String waitForResponse() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("don't interrupt me when I'm talking", e);
        }
        latch = new CountDownLatch(1);
        String response = messages.get(sessions.keySet().iterator().next());
        System.out.println("response = " + response);
        return response;
    }

    @Override
    public void writeLine(String info) throws IOException {
        sessions.values().iterator().next().getAsyncRemote().sendText(info);
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.put(session.getId(), session);
        latch2.countDown();
    }

    @OnMessage
    public void handleTextMessage(Session session, String message) {
        messages.put(session.getId(), message);
        latch.countDown();
    }

    @OnClose
    public void onClose(Session session) {
        messages.remove(session.getId());
        sessions.remove(session.getId());
    }
}
