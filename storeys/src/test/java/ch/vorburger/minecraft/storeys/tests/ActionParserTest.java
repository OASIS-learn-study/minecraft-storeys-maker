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
package ch.vorburger.minecraft.storeys.tests;

import java.io.IOException;

import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.AwaitAction;
import ch.vorburger.minecraft.storeys.model.CommandAction;
import ch.vorburger.minecraft.storeys.model.MessageAction;
import ch.vorburger.minecraft.storeys.model.NarrateAction;
import ch.vorburger.minecraft.storeys.model.NopAction;
import ch.vorburger.minecraft.storeys.model.Story;
import ch.vorburger.minecraft.storeys.model.TitleAction;
import ch.vorburger.minecraft.storeys.model.parser.ClassLoaderResourceStoryRepository;
import ch.vorburger.minecraft.storeys.model.parser.CommandMapping;
import ch.vorburger.minecraft.storeys.model.parser.StoryParser;
import ch.vorburger.minecraft.storeys.model.parser.SyntaxErrorException;
import org.junit.Before;
import org.junit.Test;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActionParserTest {

    private Scheduler mockScheduler = mock(Scheduler.class);

    private final CommandMapping commandMapping = new CommandMapping(
            () -> new CommandAction(null, mockScheduler),
            () -> new NarrateAction(null),
            () -> new TitleAction(null),
            () -> new AwaitAction(null),
            () -> new MessageAction(null));

    private final StoryParser parser = new StoryParser(commandMapping);

    private final Action<?>[] emptyList = new Action[]{new NopAction(), new NopAction(), new NopAction()};

    @Before
    public void setup() {
        when(mockScheduler.createSyncExecutor(null)).thenReturn(mock(SpongeExecutorService.class));
    }

    @Test
    public void helloStory() throws IOException, SyntaxErrorException {
        String storyScript = new ClassLoaderResourceStoryRepository().getStoryScript("hello");
        Story story = parser.parse(storyScript);
        assertThat(story.getActionsList(), hasSize(23));
    }

    @Test
    public void empty() throws SyntaxErrorException {
        Story story = parser.parse("");
        assertThat(story.getActionsList(), hasItems(new NopAction()));
    }

    @Test
    public void blanks() throws SyntaxErrorException {
        Story story = parser.parse("   \n \r\n  ");
        assertThat(story.getActionsList(), hasItems(emptyList));
    }

    @Test
    public void comments() throws SyntaxErrorException {
        Story story = parser.parse("\n // Comment \r\n  ");
        assertThat(story.getActionsList(), hasItems(emptyList));
    }

    @Test
    public void titles() throws SyntaxErrorException {
        Story story = parser.parse("= Once upon a time..\n== There was a pig.");
        assertThat(story.getActionsList(), hasSize(1));
    }

}
