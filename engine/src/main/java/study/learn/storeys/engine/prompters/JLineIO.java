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

import java.io.IOException;
import java.util.List;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import study.learn.storeys.engine.Text;

public class JLineIO implements SimplePrompterIO {

    // TODO https://github.com/jline/jline3/issues/253, and show Game's variables

    // private final Terminal terminal;
    private final LineReader lineReader;

    public JLineIO() throws IOException {
        // see https://github.com/jline/jline3/wiki/Terminals
        this(TerminalBuilder.builder().system(true).build());
    }

	public JLineIO(Terminal terminal) {
        // this.terminal = terminal;
        this.lineReader = LineReaderBuilder.builder().terminal(terminal).appName("The Game!").build();
    }

	@Override
	public String readLine(String prompt, List<Text> choices) throws IOException {
        // TODO make this a "menu" with Cursor up/down... that was kind of the whole point
        for (int i = 0; i < choices.size(); i++) {
            lineReader.printAbove("    " + (i + 1) + ": " + choices.get(i).getString());
        }
		return lineReader.readLine(prompt + " ");
	}

	@Override
	public void writeLine(String info) throws IOException {
        lineReader.getTerminal().writer().println(info);
        // It's important to flush now, because this may happen at the very end
        lineReader.getTerminal().writer().flush();
	}
}
