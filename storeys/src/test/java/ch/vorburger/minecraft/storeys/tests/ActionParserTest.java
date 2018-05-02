/**
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

import static com.google.common.truth.Truth.assertThat;

import ch.vorburger.minecraft.storeys.model.parser.StoryParser;
import ch.vorburger.minecraft.storeys.model.parser.ClassLoaderResourceStoryRepository;
import ch.vorburger.minecraft.storeys.model.parser.SyntaxErrorException;
import java.io.IOException;
import org.junit.Ignore;
import org.junit.Test;

public class ActionParserTest {

    private final StoryParser parser = new StoryParser(null, null);

    @Ignore // following use of Sponge.getScheduler() CommandAction extends MainThreadAction for bug #40
    // TODO Refactoring (big) to make StoryParser decoupled from actual Minecraft code...
    @Test public void helloStory() throws IOException, SyntaxErrorException {
        assertThat(
            parser.parse(new ClassLoaderResourceStoryRepository().getStoryScript("hello")).getActionsList())
            .hasSize(10);
    }

    @Test public void empty() throws SyntaxErrorException {
        assertThat(parser.parse("").getActionsList()).isEmpty();;
    }

    @Test public void blanks() throws SyntaxErrorException {
        assertThat(parser.parse("   \n \r\n  ").getActionsList()).isEmpty();;
    }

    @Test public void comments() throws SyntaxErrorException {
        assertThat(parser.parse("\n // Comment \r\n  ").getActionsList()).isEmpty();;
    }

    @Test public void titles() throws SyntaxErrorException {
        assertThat(parser.parse(
                "= Once upon a time..\n== There was a pig.\n").getActionsList())
            .hasSize(1);
    }

}
