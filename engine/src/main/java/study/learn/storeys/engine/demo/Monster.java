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

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import study.learn.storeys.engine.Interactlet;
import study.learn.storeys.engine.Prompter;

public class Monster extends Interactlet {

    Random rand = new Random();

    int monsterHP = rand.nextInt(10) + 5;
    int monsterATT = rand.nextInt(4) + 1;

    List<String> bodyTypes = ImmutableList.of("Cow body", "Human body");
    String body = random(bodyTypes);

    List<String> headTypes = ImmutableList.of("Snake head", "Red gem looking head");
    String head = random(headTypes);

    @Override public void interact(Prompter<Void> prompter) throws IOException {
        int playerHP = 10;
        int playerATT = 3;

        prompter.await(aChoice("You have " + playerHP + " HP, and " + playerATT
            + " damage.  You are faced with a monster that has a " + body + " and a " + head
            + ". Where will you attack?",
            body, body,
            head, head));
    }

    String random(List<String> pickFrom) {
        return pickFrom.get(rand.nextInt(pickFrom.size()));
    }
}