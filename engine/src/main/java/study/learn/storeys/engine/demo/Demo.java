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
package study.learn.storeys.engine.demo;

import static study.learn.storeys.engine.Prompt.aString;
import static study.learn.storeys.engine.Prompt.anInt;
import static study.learn.storeys.engine.Prompt.bye;

import java.io.IOException;

import study.learn.storeys.engine.Interactlet;
import study.learn.storeys.engine.Prompter;

public class Demo implements Interactlet {

    @Override public void interact(Prompter<Void> prompter) throws IOException {
        prompter.await(aString("Hi there!  What's your name?"))
            .await(name -> anInt("hello, " + name + ".  How old are you?"))
            .await(age -> bye("Nice to meet you, " + age + " old.  Bye now!"));
    }
}