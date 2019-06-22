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
package study.learn.storeys.engine;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

public class MainTest {
    @Test public void testAppHasAGreeting() {
        Main classUnderTest = new Main();
        assertNotNull("app should have a greeting", classUnderTest.getGreeting());
    }

    @Test public void testPromptNumberFirst() {
        // TODO using TestPrompter
    }

    public class PromptNumberFirst implements Interactlet {
        @Override public void interact(Prompter<Void> prompter) throws IOException {
            prompter.await(Prompt.anInt("gimme a number"));
        }
    }
}
