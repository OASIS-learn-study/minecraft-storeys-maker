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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Interactlet {

    // https://github.com/vorburger/mSara/blob/9691b9893e234b3b56e52ce885bf601bb38eb6df/saraswathi1/src/ch/vorburger/saraswathi/Interactlet.java
    abstract public void interact(Prompter<Void> prompter) throws IOException;

    // The following exists to avoid static import of Prompt.aString() & Co.
    // because IDEs such as e.g. VS Code / GitPod.io have the nasty habit of removing
    // what they think are un-used static imports too fast (on any small syntax error), which is a PITA.

    protected Prompt<String> aString(String prefix) {
        return Prompt.aString(prefix);
    }

    protected Prompt<Integer> anInt(String prefix) {
        return Prompt.anInt(prefix);
    }

    protected Prompt<String> aChoice(String prefix, List<String> choices) {
        return Prompt.aChoice(prefix, choices);
    }

    protected Prompt<String> aChoice(String prefix, String id1, String label1, String id2, String label2, String... moreChoices) {
        List<String> allChoices = new ArrayList<>(moreChoices.length + 4);
        allChoices.add(id1);  allChoices.add(label1);
        allChoices.add(id2);  allChoices.add(label2);
        allChoices.addAll(Arrays.asList(moreChoices));
        return aChoice(prefix, allChoices);
    }

    protected Prompt<Void> bye(String prefix) {
        return Prompt.bye(prefix);
    }

    protected Prompt<Void> bye() {
        return Prompt.bye();
    }
}