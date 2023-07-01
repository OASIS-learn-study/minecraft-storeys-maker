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
package ch.vorburger.minecraft.storeys.model.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.vorburger.minecraft.storeys.japi.Action;
import ch.vorburger.minecraft.storeys.japi.ReadingSpeed;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionContextImpl;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionPlayer;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionWaitHelper;
import ch.vorburger.minecraft.storeys.japi.impl.actions.CommandAction;
import ch.vorburger.minecraft.storeys.japi.impl.actions.NarrateAction;
import ch.vorburger.minecraft.storeys.japi.impl.actions.Narrator;
import ch.vorburger.minecraft.storeys.japi.impl.actions.TitleAction;
import ch.vorburger.minecraft.storeys.model.AwaitAction;
import ch.vorburger.minecraft.storeys.model.DynamicAction;
import ch.vorburger.minecraft.storeys.model.LocationAction;
import ch.vorburger.minecraft.storeys.model.MessageAction;
import ch.vorburger.minecraft.storeys.model.Story;
import ch.vorburger.minecraft.storeys.plugin.PluginInstance;
import java.io.IOException;
import java.util.List;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.TitlePart;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.TaskExecutorService;
import org.spongepowered.plugin.PluginContainer;

public class StoryParserTest {

    public static StoryParser getStoryParser() {
        PluginContainer pluginInstance = mock(PluginContainer.class);
        Scheduler scheduler = mock(Scheduler.class);
        when(scheduler.executor(isA(PluginContainer.class))).thenReturn(mock(TaskExecutorService.class));

        ActionWaitHelper actionWaitHelper = new ActionWaitHelper(pluginInstance);
        CommandMapping commandMapping = new CommandMapping(() -> new CommandAction(pluginInstance, scheduler),
                () -> new NarrateAction(new Narrator(pluginInstance)), () -> new TitleAction(actionWaitHelper),
                () -> new AwaitAction(actionWaitHelper), () -> new DynamicAction(null, null), LocationAction::new,
                () -> new MessageAction(actionWaitHelper));

        return new StoryParser(commandMapping);
    }

    @Test public void helloStory() throws IOException, SyntaxErrorException {
        String storyScript = new ClassLoaderResourceStoryRepository().getStoryScript("hello");
        Story story = getStoryParser().parse(storyScript);
        assertThat(story.getActionsList(), hasSize(10));
    }

    @Test public void emptyActionList() throws SyntaxErrorException {
        Story story = getStoryParser().parse("");
        assertThat(story.getActionsList(), empty());
    }

    @Test public void blanks() throws SyntaxErrorException {
        Story story = getStoryParser().parse("   \n \r\n  ");
        assertThat(story.getActionsList(), empty());
    }

    @Test public void comments() throws SyntaxErrorException {
        Story story = getStoryParser().parse("\n // Comment \n");
        assertThat(story.getActionsList(), empty());
    }

    @Test public void titles() throws SyntaxErrorException {
        Story story = getStoryParser().parse("= Once upon a time..\n== There was a pig.\n");
        assertThat(story.getActionsList(), hasSize(1));
    }

    @Test public void parse() throws IOException, SyntaxErrorException {
        // given
        String storyText = new ClassLoaderResourceStoryRepository().getStoryScript("parse-test");
        StoryParser storyParser = getStoryParser();

        // when
        Story story = storyParser.parse(storyText);

        // then
        List<Action<?>> storyActionsList = story.getActionsList();
        assertEquals(5, storyActionsList.size());
        assertEquals(TitleAction.class, storyActionsList.get(0).getClass());
        assertEquals(MessageAction.class, storyActionsList.get(1).getClass());
        NarrateAction narrateAction = (NarrateAction) storyActionsList.get(2);
        assertEquals("Piggy", narrateAction.getEntityName());
        assertEquals(Component.text("Hi there! I'm Piggy.").append(Component.newline())
                .append(Component.text("Welcome to the storeys mod.  I'll be giving you a quick guided tour now...")), narrateAction.getText());
        assertEquals("CommandAction: /tp -235 64 230 17 12", storyActionsList.get(3).toString());
        assertEquals(NarrateAction.class, storyActionsList.get(4).getClass());
    }

    @Test public void parseDynamic() throws IOException, SyntaxErrorException {
        // given
        String storyText = new ClassLoaderResourceStoryRepository().getStoryScript("dynamic-test");
        StoryParser storyParser = getStoryParser();

        // when
        Story story = storyParser.parse(storyText);

        // then
        List<Action<?>> storyActionsList = story.getActionsList();
        assertEquals(3, storyActionsList.size());
        assertEquals(MessageAction.class, storyActionsList.get(0).getClass());
        assertEquals(DynamicAction.class, storyActionsList.get(1).getClass());
        assertEquals(MessageAction.class, storyActionsList.get(2).getClass());
    }

    @Test public void parseMessage() throws IOException, SyntaxErrorException {
        // given
        String storyText = new ClassLoaderResourceStoryRepository().getStoryScript("message-test");
        StoryParser storyParser = getStoryParser();

        // when
        Story story = storyParser.parse(storyText);

        // then
        List<Action<?>> storyActionsList = story.getActionsList();
        assertEquals(2, storyActionsList.size());
        assertEquals(MessageAction.class, storyActionsList.get(0).getClass());
        MessageAction action = (MessageAction) storyActionsList.get(1);
        assertEquals("Only to find the town almost empty.", action.getText().content());
    }

    @Test public void execute() throws IOException, SyntaxErrorException {
        // given
        String storyText = new ClassLoaderResourceStoryRepository().getStoryScript("parse-test");
        StoryParser storyParser = getStoryParser();
        Story story = storyParser.parse(storyText);

        Audience commandSource = mock(Audience.class);

        // when
        ActionPlayer storyPlayer = new ActionPlayer();
        storyPlayer.play(new ActionContextImpl(commandSource, new ReadingSpeed()), story.getActionsList());

        // then
//        verify(commandSource).sendTitlePart(any(TitlePart.class));
    }
}