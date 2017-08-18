package ch.vorburger.minecraft.storeys.narrate;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.spongepowered.api.data.key.Keys.CUSTOM_NAME_VISIBLE;
import static org.spongepowered.api.data.key.Keys.DISPLAY_NAME;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

public class Narrator {

    // TODO support narrating Text not String (but how to chop it up?)

    private static final Logger LOG = LoggerFactory.getLogger(Narrator.class);

    private final Object plugin;
    private final Splitter splitter = new Splitter();

    private final int maxLength = 10;
    private final int waitInMS = 700;

    public Narrator(Object plugin) {
        super();
        this.plugin = plugin;
    }

    public CompletionStage<Void> narrate(Entity entity, String text) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Task.builder()
            .execute(new NarratorTask(entity, splitter.split(maxLength, text), future))
            .interval(waitInMS, MILLISECONDS)
            .submit(plugin);

        return future;
    }

    private class NarratorTask implements Consumer<Task> {

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
