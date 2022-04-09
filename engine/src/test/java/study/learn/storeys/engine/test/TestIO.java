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

import java.io.IOException;
import java.util.List;
import study.learn.storeys.engine.Text;
import study.learn.storeys.engine.prompters.SimplePrompterIO;

public class TestIO implements SimplePrompterIO {

    private String nextRead;
    private String lastWritten;

    void setNextRead(String answer) {
        nextRead = answer;
    }

    String getLastWritten() {
        return lastWritten;
    }

    @Override public String readLine(String prompt, List<Text> choices) throws IOException {
        // NOT writeLine(prompt);
        String nowRead = nextRead;
        if (nowRead == null)
            throw new IllegalStateException("Must setNextRead() before asking: " + prompt);
        nextRead = null;
        return nowRead;
    }

    @Override public void writeLine(String info) throws IOException {
        this.lastWritten = info;
    }
}
