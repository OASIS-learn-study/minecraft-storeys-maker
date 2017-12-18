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

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vert.x EventBus consumer handler.
 *
 * @author Michael Vorburger.ch
 */
public class ActionsConsumer implements Handler<Message<String>> {

    private static final Logger LOG = LoggerFactory.getLogger(ActionsConsumer.class);

    @Override
    public void handle(Message<String> message) {
        LOG.info(message.body());
    }

}
