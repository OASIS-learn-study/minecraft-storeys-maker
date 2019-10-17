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

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import ch.vorburger.minecraft.storeys.StoryPlayer;
import ch.vorburger.minecraft.storeys.model.*;
import org.junit.Before;
import org.junit.Test;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.title.Title;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

public class StoryParserTest {

    @Before
    public void initialize() throws Exception {
        TestPlainTextSerializer.inject();
    }

    public static StoryParser getStoryParser() {
        PluginInstance pluginInstance = mock(PluginInstance.class);
        Scheduler scheduler = mock(Scheduler.class);
        when(scheduler.createSyncExecutor(isA(PluginInstance.class))).thenReturn(mock(SpongeExecutorService.class));

        ActionWaitHelper actionWaitHelper = new ActionWaitHelper(pluginInstance);
        List<Action> actionList = Arrays.asList(
                new CommandAction(pluginInstance, scheduler),
                new NarrateAction(new Narrator(pluginInstance)),
                new TitleAction(actionWaitHelper),
                new AwaitAction(actionWaitHelper),
                new DynamicAction(null, null),
                new LocationAction(),
                new NopAction(),
                new MessageAction(actionWaitHelper));

        return new StoryParser(actionList);
    }

    @Test
    public void parse() throws IOException, SyntaxErrorException {
        // given
        String storyText = new ClassLoaderResourceStoryRepository().getStoryScript("parse-test");
        StoryParser storyParser = getStoryParser();

        // when
        Story story = storyParser.parse(storyText);

        // then
        List<Action<?>> storyActionsList = story.getActionsList();
        assertTrue(!storyActionsList.isEmpty());
        assertEquals(TitleAction.class, storyActionsList.get(0).getClass());
        assertEquals(MessageAction.class, storyActionsList.get(1).getClass());
        assertEquals("CommandAction: /tp -235 64 230 17 12", storyActionsList.get(2).toString());
    }

    @Test
    public void parseDynamic() throws IOException, SyntaxErrorException {
        // given
        String storyText = new ClassLoaderResourceStoryRepository().getStoryScript("dynamic-test");
        StoryParser storyParser = getStoryParser();

        // when
        Story story = storyParser.parse(storyText);

        // then
        List<Action<?>> storyActionsList = story.getActionsList();
        assertEquals(1, storyActionsList.size());
        assertEquals(DynamicAction.class, storyActionsList.get(0).getClass());
    }

    @Test
    public void execute() throws IOException, SyntaxErrorException {
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