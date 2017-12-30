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

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import ch.vorburger.minecraft.storeys.events.Condition;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.events.ConditionService.ConditionServiceRegistration;
import ch.vorburger.minecraft.storeys.events.LocatableInBoxCondition;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.ActionContext;
import ch.vorburger.minecraft.storeys.model.CommandAction;
import ch.vorburger.minecraft.storeys.model.NarrateAction;
import ch.vorburger.minecraft.storeys.model.TitleAction;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * Vert.x EventBus consumer handler.
 *
 * @author Michael Vorburger.ch
 */
public class ActionsConsumer implements Handler<Message<JsonObject>> {

    private static final Logger LOG = LoggerFactory.getLogger(ActionsConsumer.class);

    private final PluginInstance plugin;
    private final Game game;
    private final Narrator narrator;
    private final ConditionService conditionService;
    private final EventBusSender eventBusSender;

    private final Map<String, ConditionServiceRegistration> conditionRegistrations = new ConcurrentHashMap<>();

    public ActionsConsumer(PluginInstance plugin, Game game, ConditionService conditionService, EventBusSender eventBusSender) {
        this.plugin = plugin;
        this.game = game;
        this.eventBusSender = eventBusSender;

        this.narrator = new Narrator(plugin);
        this.conditionService = conditionService;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        LOG.info("Handling message received on EventBus: {}", message.body().encodePrettily());

        // TODO how to obtain the current player, from some login token?
        // For now we hard-code, but this won't really fly, of course...
        Optional<Player> optPlayer = game != null ? game.getServer().getPlayer(UUID.fromString("73551f35-7acb-45c0-bc65-8083c53eec69")) : Optional.empty();

        try {
            JsonObject json = message.body();
            switch (json.getString("action")) {
            case "ping": {
                message.reply("pong");
                LOG.info("ping & pong ACK reply");
                break;
            }
            case "setTitle": {
                String text = json.getString("text");
                execute(optPlayer.get(), new TitleAction(plugin).setText(Text.of(text)), message);
                break;
            }
            case "narrate": {
                String text = json.getString("text");
                String entity = json.getString("entity");
                execute(optPlayer.get(), new NarrateAction(narrator).setEntity(entity).setText(Text.of(text)), message);
                break;
            }
            case "command": {
                String command = json.getString("command");
                execute(optPlayer.get(), new CommandAction().setCommand(command), message);
                break;
            }
            case "registerCondition": {
                registerCondition(optPlayer.get(), requireNonNull(json.getString("condition"), "condition"));
                break;
            }
            default:
                LOG.error("Unknown action in message: " + message.body().encodePrettily());
                break;
            }
        } catch (Exception e) {
            // TODO make red etc. like in that command helper
            optPlayer.ifPresent(player -> player.sendMessage(Text.of(e.getMessage())));
            throw e;
        }
    }

    private void execute(CommandSource commandSource, Action<?> action, Message<?> message) {
        action.execute(new ActionContext(commandSource, new ReadingSpeed())).thenRun(() -> message.reply("done"));
    }

    private void registerCondition(Player player, String conditionAsText) {
        String MY_PLAYER_INSIDE = "myPlayer_inside_";
        if (conditionAsText.startsWith(MY_PLAYER_INSIDE)) {
            String args = conditionAsText.substring(MY_PLAYER_INSIDE.length());
            Condition condition = new LocatableInBoxCondition(player, args);
            ConditionServiceRegistration registration = conditionService.register(condition, () -> {
                eventBusSender.send(new JsonObject().put("event", conditionAsText));
            });
            conditionRegistrations.put(conditionAsText, registration);
        } else {
            LOG.error("Unknown condition: " + conditionAsText);
        }
    }

}
