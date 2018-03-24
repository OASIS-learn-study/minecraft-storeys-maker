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

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import ch.vorburger.minecraft.storeys.events.Condition;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.events.ConditionService.ConditionServiceRegistration;
import ch.vorburger.minecraft.storeys.events.EventService;
import ch.vorburger.minecraft.storeys.events.LocatableInBoxCondition;
import ch.vorburger.minecraft.storeys.events.ScriptCommand;
import ch.vorburger.minecraft.storeys.events.Unregisterable;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.ActionContext;
import ch.vorburger.minecraft.storeys.model.CommandAction;
import ch.vorburger.minecraft.storeys.model.NarrateAction;
import ch.vorburger.minecraft.storeys.simple.Minecraft;
import ch.vorburger.minecraft.storeys.simple.Token;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.simple.TokenProvider.SecretPublicKeyPair;
import ch.vorburger.minecraft.storeys.simple.impl.NotLoggedInException;
import com.google.common.base.Splitter;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import static java.util.Objects.requireNonNull;

/**
 * Vert.x EventBus consumer handler.
 *
 * @author Michael Vorburger.ch
 */
public class ActionsConsumer implements Handler<Message<JsonObject>> {

    private static final Logger LOG = LoggerFactory.getLogger(ActionsConsumer.class);
    private static final Splitter SLASH_SPLITTER = Splitter.on('/');

    // TODO Most of these should completely move into MinecraftImpl...
    private final PluginInstance plugin;
    private final Narrator narrator;
    private final EventService eventService;
    private final ConditionService conditionService;
    private final EventBusSender eventBusSender;
    private final TokenProvider tokenProvider;
    private final Minecraft minecraft;

    private final Map<String, Unregisterable> conditionRegistrations = new ConcurrentHashMap<>();

    public ActionsConsumer(PluginInstance plugin, EventService eventService,
            ConditionService conditionService, EventBusSender eventBusSender,
            TokenProvider tokenProvider, Minecraft minecraft) {
        this.plugin = plugin;
        this.eventBusSender = eventBusSender;

        this.narrator = new Narrator(plugin);
        this.eventService = eventService;
        this.conditionService = conditionService;

        this.tokenProvider = tokenProvider;
        this.minecraft = minecraft;

        eventService.registerPlayerJoin(event -> {
            JsonObject message = new JsonObject().put("event", "playerJoined").put("player", event.getTargetEntity().getName());
            eventBusSender.send(message);
        });
    }

    @Override
    public void handle(Message<JsonObject> message) {
        LOG.info("Handling message received on EventBus: {}", message.body().encodePrettily());

        JsonObject json = message.body();
        String secureCode = json.getString("code");
        Token token = tokenProvider.getToken(secureCode);
        Optional<Player> optPlayer = tokenProvider.getOptionalPlayer(token);

        try {
            switch (json.getString("action")) {
            case "ping": {
                message.reply("pong");
                LOG.info("ping & pong ACK reply");
                break;
            }
            case "setTitle": {
                String text = json.getString("text");
                minecraft.setTitle(token, text).thenRun(() -> message.reply("done"));
                break;
            }
            case "narrate": {
                String text = json.getString("text");
                String entity = json.getString("entity");
                execute(optPlayer.orElseThrow(NotLoggedInException::new),
                        new NarrateAction(narrator).setEntity(entity).setText(Text.of(text)), message);
                break;
            }
            case "command": {
                String command = json.getString("command");
                execute(optPlayer.orElseThrow(NotLoggedInException::new),
                        new CommandAction().setCommand(command), message);
                break;
            }
            case "registerCondition": {
                registerCondition(optPlayer.orElseThrow(NotLoggedInException::new),
                                  requireNonNull(json.getString("condition"), "condition"));
                break;
            }
            case "login": {
                String code = json.getString("token");
                String key = json.getString("key");

                SecretPublicKeyPair secretAndPublicKey = tokenProvider.login(code, key);

                eventBusSender.send(new JsonObject()
                        .put("event", "loggedIn")
                        .put("secret", secretAndPublicKey.getSecret())
                        .put("key", secretAndPublicKey.getBase64PublicKey()));
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

    public void stop() {
        for (Unregisterable reg : conditionRegistrations.values()) {
            reg.unregister();
        }
    }

    private void execute(CommandSource commandSource, Action<?> action, Message<?> message) {
        action.execute(new ActionContext(commandSource, new ReadingSpeed())).thenRun(() -> message.reply("done"));
    }

    private void registerCondition(Player player, String conditionAsText) {
        if (runIfStartsWith(conditionAsText, "myPlayer_inside_", coordinates -> {
            Condition condition = new LocatableInBoxCondition(player, coordinates);
            ConditionServiceRegistration registration = conditionService.register(condition, () -> {
                eventBusSender.send(new JsonObject().put("event", conditionAsText));
            });
            conditionRegistrations.put(conditionAsText, registration);
        })) {} else if (runIfStartsWith(conditionAsText, "newCmd", commandName -> {
            ScriptCommand scriptCommand = new ScriptCommand(commandName, plugin, () -> {
                eventBusSender.send(new JsonObject().put("event", conditionAsText));
            });
            conditionRegistrations.put(conditionAsText, scriptCommand);
        })) {} else if (runIfStartsWith(conditionAsText, "entity_interaction:", entityNameSlashInteraction -> {
            Iterator<String> parts = SLASH_SPLITTER.split(entityNameSlashInteraction).iterator();
            String entityName = parts.next();
            // String interaction = parts.next();
            conditionRegistrations.put(conditionAsText, eventService.registerInteractEntity(entityName, () -> {
                eventBusSender.send(new JsonObject().put("event", conditionAsText));
            }));
        })) {} else {
            LOG.error("Unknown condition: " + conditionAsText);
        }
    }

    private boolean runIfStartsWith(String conditionAsText, String prefix, Consumer<String> consumer) {
        if (conditionAsText.startsWith(prefix)) {
            String args = conditionAsText.substring(prefix.length());
            consumer.accept(args);
            return true;
        } else {
            return false;
        }
    }

}
