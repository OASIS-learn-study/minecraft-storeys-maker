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

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import ch.vorburger.minecraft.storeys.api.HandType;
import ch.vorburger.minecraft.storeys.api.ItemType;
import ch.vorburger.minecraft.storeys.api.Minecraft;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.ActionContext;
import ch.vorburger.minecraft.storeys.model.NarrateAction;
import ch.vorburger.minecraft.storeys.model.TitleAction;
import ch.vorburger.minecraft.storeys.simple.Token;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
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

    private final PluginInstance pluginInstance;
    private final TokenProvider tokenProvider;

    public MinecraftImpl(Vertx vertx, PluginInstance pluginInstance, TokenProvider tokenProvider) {
        this.pluginInstance = pluginInstance;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void showTitle(String code, String message, Handler<AsyncResult<Void>> handler) {
        CompletionStage<Void> completionStage = execute(getPlayer(code), new TitleAction(pluginInstance).setText(Text.of(message)));
        handler.handle(new CompletionStageBasedAsyncResult<>(completionStage));
    }

    @Override
    public void narrate(String code, String entity, String text, Handler<AsyncResult<Void>> handler) {
        final NarrateAction narrateAction = new NarrateAction(new Narrator(pluginInstance));
        narrateAction.setEntity(entity).setText(Text.of(text));
        final CompletionStage<Void> completionStage = execute(getPlayer(code), narrateAction);
        handler.handle(new CompletionStageBasedAsyncResult<>(completionStage));
    }

    @Override
    public void getItemHeld(String code, HandType hand, Handler<AsyncResult<ItemType>> handler) {
        Player player = getPlayer(code);
        Optional<ItemStack> optItemStack = player.getItemInHand(hand.getCatalogType());
        ItemType itemType = optItemStack.map(ItemStack::getType).map(ItemType::getEnum).orElse(ItemType.Nothing);
        handler.handle(Future.succeededFuture(itemType));
    }

    private <T> CompletionStage<T> execute(CommandSource commandSource, Action<T> action) {
        return action.execute(new ActionContext(commandSource, new ReadingSpeed()));
    }

    private Player getPlayer(String code) {
        Token token = tokenProvider.getToken(code);
        return tokenProvider.getPlayer(token);
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
