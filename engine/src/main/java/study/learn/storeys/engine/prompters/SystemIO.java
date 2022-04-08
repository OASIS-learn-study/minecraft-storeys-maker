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
package study.learn.storeys.engine.prompters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import study.learn.storeys.engine.Text;

public class SystemIO implements SimplePrompterIO {

 private final BufferedReader input;
 private final PrintStream output;

    public SystemIO() {
        this(System.in, System.out);
    }

    public SystemIO(InputStream in, PrintStream out) {
        this.input = new BufferedReader(new InputStreamReader(in));
        this.output = out;
 }

 @Override
 public String readLine(String prompt, List<Text> choices) throws IOException {
        for (int i = 0; i < choices.size(); i++) {
            output.println("    " + (i + 1) + ": " + choices.get(i).getString());
        }
        output.print(prompt);
        output.print(' ');
        return input.readLine();
    }

    @Override
    public void writeLine(String info) {
        output.println(info);
    }
}