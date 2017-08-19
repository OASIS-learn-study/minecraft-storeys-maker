package ch.vorburger.minecraft.storeys;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.ActionContext;
import ch.vorburger.minecraft.storeys.model.Story;
import java.util.concurrent.CompletionStage;

public class StoryPlayer {

    private final PluginInstance plugin;

    public StoryPlayer(PluginInstance plugin) {
        super();
        this.plugin = plugin;
    }

    public CompletionStage<?> play(ActionContext context, Story story) {
        // TODO This must most probably run in an async (!) Task, similar to Narrator - or does it not (because Actions are already async) ?
        CompletionStage<?> previousCompletionStage = null;
        for (Action<?> action : story.getActionsList()) {
            if (previousCompletionStage != null) {
                previousCompletionStage = previousCompletionStage.thenCompose(lastResult -> action.execute(context));
            } else {
                previousCompletionStage = action.execute(context);
            }
        }
        return previousCompletionStage;
    }

}
