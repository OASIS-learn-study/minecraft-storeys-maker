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
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import ch.vorburger.minecraft.storeys.StoryPlayer;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.ActionContext;
import ch.vorburger.minecraft.storeys.model.ActionWaitHelper;
import ch.vorburger.minecraft.storeys.model.AwaitAction;
import ch.vorburger.minecraft.storeys.model.CommandAction;
import ch.vorburger.minecraft.storeys.model.DynamicAction;
import ch.vorburger.minecraft.storeys.model.LocationAction;
import ch.vorburger.minecraft.storeys.model.MessageAction;
import ch.vorburger.minecraft.storeys.model.NarrateAction;
import ch.vorburger.minecraft.storeys.model.NopAction;
import ch.vorburger.minecraft.storeys.model.Story;
import ch.vorburger.minecraft.storeys.model.TitleAction;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;

public class StoryParserTest {

    private final Action<?>[] emptyList = new Action[] { new NopAction(), new NopAction(), new NopAction() };

    @BeforeClass public static void initialize() throws Exception {
        TestPlainTextSerializer.inject();
    }

    public static StoryParser getStoryParser() {
        PluginInstance pluginInstance = mock(PluginInstance.class);
        Scheduler scheduler = mock(Scheduler.class);
        when(scheduler.createSyncExecutor(isA(PluginInstance.class))).thenReturn(mock(SpongeExecutorService.class));

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
        assertEquals(Text.of("Hi there! I'm Piggy.").concat(Text.NEW_LINE).concat(Text.of("Welcome to the storeys mod.  I'll be giving you a quick guided tour now...")),
                narrateAction.getText());
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
        assertEquals("Only to find the town almost empty.", action.getText().toPlain());
    }

    @Test public void execute() throws IOException, SyntaxErrorException {
        // given
        String storyText = new ClassLoaderResourceStoryRepository().getStoryScript("parse-test");
        StoryParser storyParser = getStoryParser();
        Story story = storyParser.parse(storyText);

        Player commandSource = mock(Player.class);

        // when
        StoryPlayer storyPlayer = new StoryPlayer();
        storyPlayer.play(new ActionContext(commandSource, new ReadingSpeed()), story);

        // then
        verify(commandSource).sendTitle(any(Title.class));
    }
}