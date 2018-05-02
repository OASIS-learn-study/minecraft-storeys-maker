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
package ch.vorburger.minecraft.storeys.model;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.spotify.futures.CompletableFuturesExtra;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.SpongeExecutorService;

/**
 * Action which must run on the Game's main thread. Any actions that interact
 * with Sponge (<a href=
 * "https://docs.spongepowered.org/stable/en/plugin/scheduler.html#asynchronous-tasks">except
 * Chat & Permissions</a>) must extend this.
 */
public abstract class MainThreadAction<T> implements Action<T> {

    private final ListeningScheduledExecutorService guavaifiedMinecraftExecutor;

    protected MainThreadAction(PluginInstance plugin) {
        SpongeExecutorService minecraftExecutor = Sponge.getScheduler().createSyncExecutor(plugin);
        guavaifiedMinecraftExecutor = MoreExecutors.listeningDecorator(minecraftExecutor);
    }

    abstract protected T executeInMainThread(ActionContext context) throws ActionException;

    @Override
    public CompletionStage<T> execute(ActionContext context) {
        ListenableFuture<T> future = guavaifiedMinecraftExecutor.submit(() -> executeInMainThread(context));
        return CompletableFuturesExtra.toCompletableFuture(future);
    }

}
