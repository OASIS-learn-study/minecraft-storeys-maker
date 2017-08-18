package ch.vorburger.minecraft.storeys;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.Story;
import ch.vorburger.minecraft.storeys.narrate.Narrator;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.command.CommandSource;

public class StoryPlayer {

    private final Narrator narrator;

    public StoryPlayer(PluginInstance plugin) {
        narrator = new Narrator(plugin);
    }

    public void play(CommandSource src, Story story) {
        // TODO Must this be async, like Narrator?
        for (Action<?> action : story.getActionsList()) {
            // TODO Correct chain the futures ..
            CompletionStage<?> future = action.execute(src);
        }
    }

}
