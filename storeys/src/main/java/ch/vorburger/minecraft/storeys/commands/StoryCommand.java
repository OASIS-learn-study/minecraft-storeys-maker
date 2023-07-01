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
package ch.vorburger.minecraft.storeys.commands;

import ch.vorburger.minecraft.storeys.japi.ReadingSpeed;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionContextImpl;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionPlayer;
import ch.vorburger.minecraft.storeys.japi.util.CommandExceptions;
import ch.vorburger.minecraft.storeys.model.Story;
import ch.vorburger.minecraft.storeys.model.parser.FileStoryRepository;
import ch.vorburger.minecraft.storeys.model.parser.StoryParser;
import ch.vorburger.minecraft.storeys.model.parser.StoryRepository;
import ch.vorburger.minecraft.storeys.util.Command;
import java.io.File;
import java.nio.file.Path;
import javax.inject.Inject;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

public class StoryCommand implements Command {

    private static final Parameter.Value<String> STORY_NAME = Parameter.string().key("storyName").build();

    private final StoryRepository storyRepository;
    private final StoryParser storyParser;
    private final ActionPlayer storyPlayer;

    @Inject public StoryCommand(Path configDir, StoryParser storyParser, ActionPlayer storyPlayer) {
        File storiesDir = new File(configDir.toFile(), "stories");
        if (!storiesDir.exists()) {
            storiesDir.mkdirs();
        }
        storyRepository = new FileStoryRepository(storiesDir);
        this.storyParser = storyParser;
        this.storyPlayer = storyPlayer;
    }

    @Override public String getName() {
        return "story";
    }

    @Override public org.spongepowered.api.command.Command.Parameterized createCommand() {
        return org.spongepowered.api.command.Command.builder().shortDescription(Component.text("Tell a story"))
                // .permission("storeys.commands.story") ?
                .addParameter(STORY_NAME) // TODO requiringPermission()
                .executor(this).build();
    }

    @Override public CommandResult execute(CommandContext commandContext) throws CommandException {
        String storyName = commandContext.requireOne(STORY_NAME);

        CommandExceptions.doOrThrow("Failed to load & play '" + storyName + "' story, due to: ", () -> {
            String storyScript = storyRepository.getStoryScript(storyName);
            Story story = storyParser.parse(storyScript);
            /* CompletionStage<?> completionStage = storyPlayer.play(..) */ // TODO keep this, so that a user can /stop the story again..
            storyPlayer.play(new ActionContextImpl(commandContext.cause().audience(), new ReadingSpeed()), story.getActionsList());
        });

        return CommandResult.success();
    }
}
