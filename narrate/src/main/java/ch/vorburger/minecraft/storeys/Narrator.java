package ch.vorburger.minecraft.storeys;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.spongepowered.api.data.key.Keys.CUSTOM_NAME_VISIBLE;
import static org.spongepowered.api.data.key.Keys.DISPLAY_NAME;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.util.NamedObjects;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.extent.EntityUniverse;

public class Narrator {

    // TODO support narrating Text not String (but how to chop it up?)

    private final NamedObjects namedObjects = new NamedObjects();

    private final PluginInstance plugin;
    private final TextSplitter splitter = new TextSplitter();

    private final int maxLength = 20;

    public Narrator(PluginInstance plugin) {
        super();
        this.plugin = plugin;
    }

    public CompletionStage<Void> narrate(EntityUniverse entityUniverse, String entityName, String text, ReadingSpeed readingSpeed) {
        Entity entity = namedObjects.getEntity(entityUniverse, entityName).orElseThrow(() -> new IllegalArgumentException("No entity named: " + entityName));
        return narrate(entity, text, readingSpeed);
    }

    public CompletionStage<Void> narrate(Entity entity, String text, ReadingSpeed readingSpeed) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Task.builder()
            .execute(new NarratorTask(entity, splitter.split(maxLength, text), future))
            .interval(readingSpeed.msToRead(maxLength), MILLISECONDS)
            .submit(plugin);

        return future;
    }

    private static class NarratorTask implements Consumer<Task> {

        private final Entity entity;
        private final Iterator<String> splitText;
        private final CompletableFuture<Void> future;
        private final Optional<Text> originalDisplayName;

        public NarratorTask(Entity entity, Iterable<String> splitText, CompletableFuture<Void> future) {
            this.entity = entity;
            this.splitText = splitText.iterator();
            this.future = future;

            // Make sure name can always be seen, even if we are not closely look at entity
            entity.offer(CUSTOM_NAME_VISIBLE, true);

            originalDisplayName = entity.get(DISPLAY_NAME);
        }

        @Override
        public void accept(Task task) {
            if (splitText.hasNext()) {
                entity.offer(DISPLAY_NAME, Text.of(splitText.next()));
            } else {
                entity.offer(CUSTOM_NAME_VISIBLE, false);
                // Must reset name, so that NamedObjects can find Entity again next time (after restart)
                entity.offer(DISPLAY_NAME, originalDisplayName.orElse(Text.EMPTY));
                future.complete(null);
                task.cancel();
            }
        }
    }

}
