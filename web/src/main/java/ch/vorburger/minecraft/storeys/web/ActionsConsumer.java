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

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.ActionContext;
import ch.vorburger.minecraft.storeys.model.NarrateAction;
import ch.vorburger.minecraft.storeys.model.TitleAction;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

/**
 * Vert.x EventBus consumer handler.
 *
 * @author Michael Vorburger.ch
 */
public class ActionsConsumer implements Handler<Message<JsonObject>> {

    private static final Logger LOG = LoggerFactory.getLogger(ActionsConsumer.class);

    private final PluginInstance plugin;
    private final Narrator narrator;
    private final Game game;

    public ActionsConsumer(PluginInstance plugin, Game game) {
        this.plugin = plugin;
        this.narrator = new Narrator(plugin);
        this.game = game;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        LOG.info(message.body().encodePrettily());
        JsonObject json = message.body();
        switch (json.getString("action")) {
        case "ping": {
            message.reply("pong");
            LOG.info("ping & pong ACK reply");
            break;
        }
        case "setTitle": {
            String text = json.getString("text");
            execute(new TitleAction(plugin).setText(Text.of(text)), message);
            break;
        }
        case "narrate": {
            String text = json.getString("text");
            String entity = json.getString("entity");
            execute(new NarrateAction(narrator).setEntity(entity).setText(Text.of(text)), message);
        }
        default:
            break;
        }
    }

    private void execute(Action<Void> action, Message<?> message) {
        // TODO how to obtain the current player, from some login token?
        // For now we hard-code, but this won't really fly, of course...
        CommandSource commandSource = game != null ? game.getServer().getPlayer("michaelpapa7").get() : null;
        action.execute(new ActionContext(commandSource, new ReadingSpeed())).thenRun(() -> message.reply("done"));
    }

}
