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
package ch.vorburger.minecraft.storeys.model;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.spongepowered.api.scheduler.Task;

public class ActionWaitHelper {

    private final PluginInstance plugin;

    @Inject
    public ActionWaitHelper(PluginInstance plugin) {
        this.plugin = plugin;
    }

    public <T> CompletionStage<T> executeAndWait(int msToWaitAfterRunning, Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        try {
            T returnValue = callable.call();
            Task.builder()
                .async()
                .execute(() -> future.complete(returnValue))
                .delay(msToWaitAfterRunning, MILLISECONDS)
                .submit(plugin);

        } catch (Throwable throwable) {
            future.completeExceptionally(throwable);
        }
        return future;
    }

}
