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
package ch.vorburger.minecraft.storeys.api.impl;

import static java.util.Objects.requireNonNull;

import ch.vorburger.minecraft.storeys.api.HandType;
import ch.vorburger.minecraft.storeys.api.ItemType;
import ch.vorburger.minecraft.storeys.api.Minecraft;
import ch.vorburger.minecraft.storeys.japi.impl.actions.Action;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionContext;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ReadingSpeed;
import ch.vorburger.minecraft.storeys.japi.impl.actions.TitleAction;
import ch.vorburger.minecraft.storeys.model.CommandAction;
import ch.vorburger.minecraft.storeys.model.LocationToolAction;
import ch.vorburger.minecraft.storeys.model.NarrateAction;
import ch.vorburger.minecraft.storeys.simple.impl.NotLoggedInException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Provider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;

/**
 * Implementation of {@link Minecraft} Vert.x RPC service.
 *
 * @author Michael Vorburger.ch
 */
public class MinecraftImpl implements Minecraft {

    private final Provider<TitleAction> titleActionProvider;
    private final Provider<NarrateAction> narrateActionProvider;
    private final Provider<CommandAction> commandActionProvider;

    @Inject public MinecraftImpl(Provider<TitleAction> titleActionProvider, Provider<NarrateAction> narrateActionProvider,
            Provider<CommandAction> commandActionProvider) {
        this.titleActionProvider = titleActionProvider;
        this.narrateActionProvider = narrateActionProvider;
        this.commandActionProvider = commandActionProvider;
    }

    @Override public void showTitle(String playerUUID, String message, Handler<AsyncResult<Void>> handler) {
        CompletionStage<Void> completionStage = execute(getPlayer(playerUUID), titleActionProvider.get().setText(Text.of(message)));
        handler.handle(new CompletionStageBasedAsyncResult<>(completionStage));
    }

    @Override public void narrate(String playerUUID, String entity, String text, Handler<AsyncResult<Void>> handler) {
        final NarrateAction narrateAction = narrateActionProvider.get();
        narrateAction.setEntity(entity).setText(Text.of(text));
        handler.handle(new CompletionStageBasedAsyncResult<>(execute(getPlayer(playerUUID), narrateAction)));
    }

    @Override public void runCommand(String playerUUID, String command, Handler<AsyncResult<Void>> handler) {
        CompletionStage<CommandResult> completionStageWithResult = execute(getPlayer(playerUUID),
                commandActionProvider.get().setCommand(command));
        CompletionStage<Void> voidCompletionStage = completionStageWithResult.thenAccept(commandResult -> {
            /* ignore */ });
        handler.handle(new CompletionStageBasedAsyncResult<>(voidCompletionStage));
    }

    @Override public void getItemHeld(String playerUUID, HandType hand, Handler<AsyncResult<ItemType>> handler) {
        Player player = getPlayer(playerUUID);
        Optional<ItemStack> optItemStack = player.getItemInHand(hand.getCatalogType());
        ItemType itemType = optItemStack.map(ItemStack::getType).map(ItemType::getEnum).orElse(ItemType.Nothing);
        handler.handle(Future.succeededFuture(itemType));
    }

    @Override public void addRemoveItem(String playerUUID, int amount, ItemType item, Handler<AsyncResult<Void>> handler) {
        Player player = getPlayer(playerUUID);
        item.getCatalogType().ifPresent(itemType -> {
            if (amount < 0) {
                final Inventory inventory = player.getInventory().query(QueryOperationTypes.ITEM_TYPE.of(itemType));
                inventory.poll(Math.abs(amount));
            } else {
                final ItemStack itemStack = ItemStack.builder().itemType(itemType).quantity(amount).build();
                player.getInventory().offer(itemStack);
            }
        });

        handler.handle(Future.succeededFuture());
    }

    @Override public void whenInside(String playerUUID, String name, Handler<AsyncResult<Void>> handler) {
        final LocationToolAction locationToolAction = new LocationToolAction(name);
        try {
            final CompletionStage<Void> completionStage = execute(getPlayer(playerUUID), locationToolAction);
            handler.handle(new CompletionStageBasedAsyncResult<>(completionStage));
        } catch (NotLoggedInException e) {
            // running a server side scratch file
            handler.handle(Future.succeededFuture());
        }
    }

    private <T> CompletionStage<T> execute(CommandSource commandSource, Action<T> action) {
        return action.execute(new ActionContext(commandSource, new ReadingSpeed()));
    }

    private Player getPlayer(String playerUUID) {
        requireNonNull(playerUUID, "playerUUID null");
        return Sponge.getGame().getServer().getPlayer(UUID.fromString(playerUUID)).orElseThrow(() -> new NotLoggedInException(playerUUID));
    }

    // TODO does a helper class like this already exist somewhere in Vert.x? Can Vert.x directly gen. code with
    // CompletionStage or CompletableFuture signatures?
    private static class CompletionStageBasedAsyncResult<T> implements AsyncResult<T> {

        private T result;
        private Throwable cause;
        private boolean isHandled = false;

        CompletionStageBasedAsyncResult(CompletionStage<T> completionStage) {
            completionStage.handle((newResult, newCause) -> {
                synchronized (this) {
                    CompletionStageBasedAsyncResult.this.result = newResult;
                    CompletionStageBasedAsyncResult.this.cause = newCause;
                    CompletionStageBasedAsyncResult.this.isHandled = true;
                }
                return null;
            });
        }

        @Override public synchronized T result() {
            return result;
        }

        @Override public synchronized Throwable cause() {
            return cause;
        }

        @Override public synchronized boolean succeeded() {
            return isHandled && cause == null;
        }

        @Override public synchronized boolean failed() {
            return cause != null;
        }
    }
}
