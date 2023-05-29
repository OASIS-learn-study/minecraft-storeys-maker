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
package ch.vorburger.minecraft.storeys.japi.impl.actions;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.spongepowered.api.data.Keys.DISPLAY_NAME;
import static org.spongepowered.api.data.Keys.IS_CUSTOM_NAME_VISIBLE;

import ch.vorburger.minecraft.storeys.japi.ReadingSpeed;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.volume.entity.EntityVolume;
import org.spongepowered.plugin.PluginContainer;

public class Narrator {

    // TODO support narrating Text not String (but how to chop it up?)

    private final NamedObjects namedObjects = new NamedObjects();

    private final PluginContainer plugin;
    private final TextSplitter splitter = new TextSplitter();

    private final int maxLength = 20;

    @Inject public Narrator(PluginContainer plugin) {
        super();
        this.plugin = plugin;
    }

    public CompletionStage<Void> narrate(EntityVolume.Modifiable entityUniverse, String entityName, String text, ReadingSpeed readingSpeed) {
        Entity entity = namedObjects.getEntity(entityUniverse, entityName)
                .orElseThrow(() -> new IllegalArgumentException("No entity named: " + entityName));
        return narrate(entity, text, readingSpeed);
    }

    public CompletionStage<Void> narrate(Entity entity, String text, ReadingSpeed readingSpeed) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Task.builder().execute(new NarratorTask(entity, splitter.split(maxLength, text), future))
                .interval(readingSpeed.msToRead(maxLength), MILLISECONDS).plugin(plugin);

        return future;
    }

    private static class NarratorTask implements Runnable {

        private final Entity entity;
        private final Iterator<String> splitText;
        private final CompletableFuture<Void> future;
        private final Value.Mutable<Component> originalDisplayName;

        public NarratorTask(Entity entity, Iterable<String> splitText, CompletableFuture<Void> future) {
            this.entity = entity;
            this.splitText = splitText.iterator();
            this.future = future;

            // Make sure name can always be seen, even if we are not closely look at entity
            entity.offer(IS_CUSTOM_NAME_VISIBLE, true);

            originalDisplayName = entity.displayName();
        }

        @Override public void run() {
            if (splitText.hasNext()) {
                entity.offer(DISPLAY_NAME, Component.text(splitText.next()));
            } else {
                entity.offer(IS_CUSTOM_NAME_VISIBLE, false);
                // Must reset name, so that NamedObjects can find Entity again next time (after restart)
                entity.offer(DISPLAY_NAME, originalDisplayName.get());
                future.complete(null);
                // task.cancel();
            }
        }
    }

}
