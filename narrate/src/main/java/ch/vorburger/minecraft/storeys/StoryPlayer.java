package ch.vorburger.minecraft.storeys;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.Story;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.command.CommandSource;

public class StoryPlayer {

    private final PluginInstance plugin;

    public StoryPlayer(PluginInstance plugin) {
        super();
        this.plugin = plugin;
    }

    public CompletionStage<?> play(CommandSource src, Story story) {
        // TODO This must most probably run in an async (!) Task, similar to Narrator
        CompletionStage<?> previousCompletionStage = null;
        for (Action<?> action : story.getActionsList()) {
            if (previousCompletionStage != null) {
                previousCompletionStage = previousCompletionStage.thenCompose(lastResult -> action.execute(src));
            } else {
                previousCompletionStage = action.execute(src);
            }
        }
        return previousCompletionStage;
    }

}
