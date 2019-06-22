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
package study.learn.storeys.engine.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import study.learn.storeys.engine.Interactlet;
import study.learn.storeys.engine.Prompt;
import study.learn.storeys.engine.Prompter;
import study.learn.storeys.engine.prompters.SimplePrompter;

public class EngineTest {

    TestIO testIO = new TestIO();
    Prompter<Void> prompter = new SimplePrompter<Void>(testIO);

    class EchoInteractlet implements Interactlet {
        @Override public void interact(Prompter<Void> prompter) throws IOException {
            prompter.await(Prompt.aString("Say something, I'll echo it:"))
                .await(reply -> Prompt.bye(reply));
        }
    }

    @Test public void testEcho() throws IOException {
        testIO.setNextRead("hello, world");
        new EchoInteractlet().interact(prompter);
        assertEquals("hello, world", testIO.getLastWritten());
    }

    class PromptNumberFirst implements Interactlet {
        @Override public void interact(Prompter<Void> prompter) throws IOException {
            prompter.await(Prompt.anInt("gimme a number"));
        }
    }

    @Test public void testPromptNumberFirst() throws IOException {
        testIO.setNextRead("123");
        new PromptNumberFirst().interact(prompter);
    }
}
