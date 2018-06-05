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
package ch.vorburger.minecraft.storeys.web.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ch.vorburger.minecraft.storeys.api.HandType;
import ch.vorburger.minecraft.storeys.api.ItemType;
import ch.vorburger.minecraft.storeys.api.LoginResponse;
import java.util.concurrent.atomic.AtomicReference;
import ch.vorburger.minecraft.storeys.api.CommandRegistration;
import ch.vorburger.minecraft.storeys.api.Minecraft;
import ch.vorburger.minecraft.storeys.api.Token;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Minecraft} suitable for testing.
 *
 * @author Michael Vorburger.ch
 */
public class TestMinecraft implements Minecraft {

    private static final Logger LOG = LoggerFactory.getLogger(TestMinecraft.class);

    public Map<String, String> results = new ConcurrentHashMap<>();
    public Map<HandType, ItemType> itemsHeld = new ConcurrentHashMap<>();

    @Override
    public void login(String token, String key, Handler<AsyncResult<LoginResponse>> handler) {
    }

    private final Map<String, TestCommandImpl> commandInvocationHandlers = new ConcurrentHashMap<>();

    public void invokeCommand(String commandName) {
        TestCommandImpl registration = commandInvocationHandlers.get(commandName);
        if (registration != null) {
            registration.handle();
            LOG.info("invokeCommand({}) found and called handler", commandName);
        } else {
            LOG.error("invokeCommand() found no handler for: {}", commandName);
        }
    }

    @Override
    public void newCommand(String code, String commandName, Handler<AsyncResult<CommandRegistration>> handler) {
        TestCommandImpl commandRegistration = new TestCommandImpl();
        commandInvocationHandlers.put(commandName, commandRegistration);
        LOG.info("whenCommand({}) registered handler", code, commandName);
        handler.handle(Future.succeededFuture(commandRegistration));
    }

    @Override
    public void showTitle(Token token, String message, Handler<AsyncResult<Void>> handler) {
        LOG.info("showTitle({}, {})", token, message);
        results.put("lastTitle", message);
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void narrate(String code, String entity, String text, Handler<AsyncResult<Void>> handler) {
        LOG.info("narrate({}, {}, {})", code, entity, text);
        results.put("entity", entity);
        results.put("text", text);
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void getItemHeld(String code, HandType hand, Handler<AsyncResult<ItemType>> handler) {
        handler.handle(Future.succeededFuture(itemsHeld.getOrDefault(hand, ItemType.Nothing)));
    }

    // TODO move this somewhere where it can shared with the real implementation
    public static class TestCommandImpl implements CommandRegistration {

        private final AtomicReference<Handler<AsyncResult<Void>>> handlerRef = new AtomicReference<>();

        @Override
        public void on(Handler<AsyncResult<Void>> newHandler) {
            if (this.handlerRef.getAndSet(newHandler) != null) {
                throw new IllegalStateException("handler was already set");
            }
        }

        public void handle() {
            handlerRef.get().handle(Future.succeededFuture());
        }

        @Override
        public void unregister() {
            // TODO
        }
    }
}
