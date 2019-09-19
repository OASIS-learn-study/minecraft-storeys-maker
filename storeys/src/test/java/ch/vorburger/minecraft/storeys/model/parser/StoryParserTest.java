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

import java.io.IOException;
import java.util.List;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import ch.vorburger.minecraft.storeys.StoryPlayer;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.ActionContext;
import ch.vorburger.minecraft.storeys.model.DynamicAction;
import ch.vorburger.minecraft.storeys.model.MessageAction;
import ch.vorburger.minecraft.storeys.model.Story;
import ch.vorburger.minecraft.storeys.model.TitleAction;
import org.junit.Before;
import org.junit.Test;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.title.Title;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StoryParserTest {

    @Before
    public void initialize() throws Exception {
        TestPlainTextSerializer.inject();
    }

    private StoryParser getStoryParser() {
        PluginInstance pluginInstance = mock(PluginInstance.class);
        SpongeExecutorService executorService = mock(SpongeExecutorService.class);
        return new StoryParser(pluginInstance, new Narrator(pluginInstance), executorService);
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
        StoryPlayer storyPlayer = new StoryPlayer(null);
        storyPlayer.play(new ActionContext(commandSource, new ReadingSpeed()), story);

        // then
        verify(commandSource).sendTitle(any(Title.class));
    }
}