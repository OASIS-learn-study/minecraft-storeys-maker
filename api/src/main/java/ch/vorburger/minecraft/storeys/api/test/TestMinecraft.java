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
package ch.vorburger.minecraft.storeys.api.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ch.vorburger.minecraft.storeys.api.CommandRegistration;
import ch.vorburger.minecraft.storeys.api.Minecraft;
import ch.vorburger.minecraft.storeys.api.impl.CommandRegistrationImpl;
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

    private final Map<String, CommandRegistrationImpl> commandInvocationHandlers = new ConcurrentHashMap<>();

    public void invokeCommand(String commandName) {
        CommandRegistrationImpl registration = commandInvocationHandlers.get(commandName);
        if (registration != null) {
            registration.handle();
            LOG.info("invokeCommand({}) found and called handler", commandName);
        } else {
            throw new IllegalStateException("invokeCommand() found no handler for command: " + commandName);
        }
    }

    @Override
    public void newCommand(String code, String commandName, Handler<AsyncResult<CommandRegistration>> handler) {
        CommandRegistrationImpl commandRegistration = new CommandRegistrationImpl();
        commandInvocationHandlers.put(commandName, commandRegistration);
        handler.handle(Future.succeededFuture(commandRegistration));
        LOG.info("newCommand({}) registered handler for command: {}", code, commandName);
    }

    @Override
    public void showTitle(String code, String message, Handler<AsyncResult<Void>> handler) {
        LOG.info("showTitle({}, {})", code, message);
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

}
