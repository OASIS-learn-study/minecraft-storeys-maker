package ch.vorburger.minecraft.storeys.narrate.commands;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.text.Text.of;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import ch.vorburger.minecraft.storeys.StoryPlayer;
import ch.vorburger.minecraft.storeys.model.ActionContext;
import ch.vorburger.minecraft.storeys.model.Story;
import ch.vorburger.minecraft.storeys.model.parser.FileStoryRepository;
import ch.vorburger.minecraft.storeys.model.parser.StoryParser;
import ch.vorburger.minecraft.storeys.model.parser.StoryRepository;
import ch.vorburger.minecraft.storeys.util.Command;
import ch.vorburger.minecraft.utils.CommandExceptions;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class StoryCommand implements Command {

    private static final Text ARG_STORY = of("storyName");

    private final StoryRepository storyRepository;
    private final StoryParser actionParser;
    private final StoryPlayer storyPlayer;

    public StoryCommand(PluginInstance plugin, Path configDir) {
        File storiesDir = new File(configDir.toFile(), "stories");
        if (!storiesDir.exists()) {
            storiesDir.mkdirs();
        }
        storyRepository = new FileStoryRepository(storiesDir );
        actionParser = new StoryParser(plugin, new Narrator(plugin));
        storyPlayer = new StoryPlayer(plugin);
    }

    @Override
    public List<String> aliases() {
        return ImmutableList.of("story");
    }

    @Override
    public CommandCallable callable() {
        return CommandSpec.builder()
            .description(of("Tell a story"))
            // .permission("storeys.commands.story") ?
            .arguments(
                onlyOne(string(ARG_STORY)) // TODO requiringPermission()
            ).executor(this).build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String storyName = args.<String>getOne(ARG_STORY).get();

        CommandExceptions.doOrThrow("Failed to load & play '" + storyName + "' story, due to: ", () -> {
            String storyScript = storyRepository.getStoryScript(storyName);
            Story story = actionParser.parse(storyScript);
            /* CompletionStage<?> completionStage = storyPlayer.play(..) */ // TODO keep this, so that a user can /stop the story again..
            storyPlayer.play(new ActionContext(src, new ReadingSpeed()), story);
        });

        return CommandResult.success();
    }
}
