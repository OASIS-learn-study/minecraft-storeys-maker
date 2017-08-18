package ch.vorburger.minecraft.storeys.narrate.commands;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.StoryPlayer;
import ch.vorburger.minecraft.storeys.model.Story;
import ch.vorburger.minecraft.storeys.model.parser.StoryParser;
import ch.vorburger.minecraft.storeys.model.parser.ClassLoaderResourceStoryRepository;
import ch.vorburger.minecraft.storeys.model.parser.StoryRepository;
import ch.vorburger.minecraft.storeys.narrate.Narrator;
import ch.vorburger.minecraft.storeys.util.Command;
import ch.vorburger.minecraft.utils.CommandExceptions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class NarrateCommand implements Command {

    // TODO Use new FileStoryRepository(new File("stories"));
    // TODO but correctly read configuration file directory..
    private final StoryRepository storyRepository = new ClassLoaderResourceStoryRepository();
    private final StoryParser actionParser;
    private final StoryPlayer storyPlayer;

    public NarrateCommand(PluginInstance plugin) {
        storyPlayer = new StoryPlayer(plugin);
        actionParser = new StoryParser(new Narrator(plugin));
    }

    @Override
    public List<String> aliases() {
        return ImmutableList.of("narrate", "story");
    }

    @Override
    public CommandCallable callable() {
        return CommandSpec.builder()
            .description(Text.of("Narrate a story"))
            .executor(this).build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        // TODO read story name from args
        String storyName = "hello";

        CommandExceptions.doOrThrow("Load & play story: " + storyName, () -> {
            String storyScript = storyRepository.getStoryScript(storyName);
            Story story = actionParser.parse(storyScript);
            storyPlayer.play(src, story);
        });

        return CommandResult.success();
    }
}
