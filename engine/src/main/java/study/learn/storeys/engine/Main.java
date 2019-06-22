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

import java.io.IOException;

import study.learn.storeys.engine.demo.Demo;
import study.learn.storeys.engine.prompters.JavaConsolePrompter;

public class Main {

    // TODO remove, when test is rewritten
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) throws IOException {
        Prompter<Void> prompter = new JavaConsolePrompter<Void>();
        Interactlet interactlet = new Demo();
        interactlet.interact(prompter);
    }
}
