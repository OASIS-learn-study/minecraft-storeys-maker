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
package ch.vorburger.minecraft.storeys.api.impl;

import ch.vorburger.minecraft.storeys.api.CommandRegistration;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import java.util.concurrent.atomic.AtomicReference;

public class CommandRegistrationImpl implements CommandRegistration {

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
    }
}
