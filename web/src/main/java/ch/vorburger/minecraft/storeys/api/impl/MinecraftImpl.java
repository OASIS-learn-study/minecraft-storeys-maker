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

import static java.util.Objects.requireNonNull;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import ch.vorburger.minecraft.storeys.api.CommandRegistration;
import ch.vorburger.minecraft.storeys.api.HandType;
import ch.vorburger.minecraft.storeys.api.ItemType;
import ch.vorburger.minecraft.storeys.api.LoginResponse;
import ch.vorburger.minecraft.storeys.api.Minecraft;
import ch.vorburger.minecraft.storeys.api.Token;
import ch.vorburger.minecraft.storeys.events.ScriptCommand;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.ActionContext;
import ch.vorburger.minecraft.storeys.model.NarrateAction;
import ch.vorburger.minecraft.storeys.model.TitleAction;
import ch.vorburger.minecraft.storeys.simple.TokenProvider.SecretPublicKeyPair;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

/**
 * Implementation of {@link Minecraft} Vert.x RPC service.
 *
 * @author Michael Vorburger.ch
 */
public class MinecraftImpl implements Minecraft {

    private static final Logger LOG = LoggerFactory.getLogger(MinecraftImpl.class);

    private final PluginInstance pluginInstance;
    private final ch.vorburger.minecraft.storeys.simple.TokenProvider oldTokenProvider;
    private final TokenProvider newTokenProvider;

    public MinecraftImpl(Vertx vertx, PluginInstance pluginInstance, ch.vorburger.minecraft.storeys.simple.TokenProvider oldTokenProvider, TokenProvider newTokenProvider) {
        this.pluginInstance = pluginInstance;
        this.oldTokenProvider = oldTokenProvider;
        this.newTokenProvider = newTokenProvider;
    }

    @Override
    public void login(String token, String key, Handler<AsyncResult<LoginResponse>> handler) {
        SecretPublicKeyPair secretAndPublicKey = oldTokenProvider.login(token, key);
        LoginResponse response = new LoginResponse();
        response.setSecret(secretAndPublicKey.getSecret());
        response.setKey(secretAndPublicKey.getBase64PublicKey());
        LOG.info("login: IN token={}, key={}, OUT response={}", token, key, response);
        handler.handle(Future.succeededFuture(response));
    }

    @Override
    public void showTitle(Token token, String message, Handler<AsyncResult<Void>> handler) {
        CompletionStage<Void> completionStage = execute(getPlayer(token), new TitleAction(pluginInstance).setText(Text.of(message)));
        handler.handle(new CompletionStageBasedAsyncResult<>(completionStage));
    }

    @Override
    public void narrate(String code, String entity, String text, Handler<AsyncResult<Void>> handler) {
        final NarrateAction narrateAction = new NarrateAction(new Narrator(pluginInstance));
        narrateAction.setEntity(entity).setText(Text.of(text));
        handler.handle(new CompletionStageBasedAsyncResult<>(execute(getPlayer(code), narrateAction)));
    }

    @Override
    public void getItemHeld(String code, HandType hand, Handler<AsyncResult<ItemType>> handler) {
        Player player = getPlayer(code);
        Optional<ItemStack> optItemStack = player.getItemInHand(hand.getCatalogType());
        ItemType itemType = optItemStack.map(ItemStack::getType).map(ItemType::getEnum).orElse(ItemType.Nothing);
        handler.handle(Future.succeededFuture(itemType));
    }

    @Override
    public void newCommand(String code, String commandName, Handler<AsyncResult<CommandRegistration>> handler) {
        LOG.info("newCommand: {}", commandName); // TODO remove
        AtomicReference<ScriptCommand> commandRef = new AtomicReference<>();
        CommandRegistrationImpl commandRegistration = new CommandRegistrationImpl() {
            @Override
            public void unregister() {
                ScriptCommand command = commandRef.get();
                if (command != null) {
                    command.unregister();
                }
            }
        };
        commandRef.set(new ScriptCommand(commandName, pluginInstance, () -> {
            commandRegistration.handle();
        }));
        handler.handle(Future.succeededFuture(commandRegistration));
    }

    private <T> CompletionStage<T> execute(CommandSource commandSource, Action<T> action) {
        return action.execute(new ActionContext(commandSource, new ReadingSpeed()));
    }

    private Player getPlayer(Token token) {
        if (requireNonNull(token, "token").getLoginCode() != null) {
            return getPlayer(token.getLoginCode());
        } else if (token.getPlayerSource() != null) {
            return newTokenProvider.getPlayer(token);
        } else {
            throw new IllegalArgumentException("Token JSON contains neither loginCode nor playerSource");
        }
    }

    private Player getPlayer(String code) {
        ch.vorburger.minecraft.storeys.simple.Token token = oldTokenProvider.getToken(code);
        return oldTokenProvider.getPlayer(token);
    }

    // TODO does a helper class like this already exist somewhere in Vert.x? Can Vert.x directly gen. code with CompletionStage or CompletableFuture signatures?
    private static class CompletionStageBasedAsyncResult<T> implements AsyncResult<T> {

        private T result;
        private Throwable cause;
        private boolean isHandled = false;

        CompletionStageBasedAsyncResult(CompletionStage<T> completionStage) {
            completionStage.handle((newResult, newCause) -> {
                synchronized(this) {
                    CompletionStageBasedAsyncResult.this.result = newResult;
                    CompletionStageBasedAsyncResult.this.cause = newCause;
                    CompletionStageBasedAsyncResult.this.isHandled = true;
                }
                return null;
            });
        }

        @Override
        public synchronized T result() {
            return result;
        }

        @Override
        public synchronized Throwable cause() {
            return cause;
        }

        @Override
        public synchronized boolean succeeded() {
            return isHandled && cause == null;
        }

        @Override
        public synchronized boolean failed() {
            return cause != null;
        }
    }
}
