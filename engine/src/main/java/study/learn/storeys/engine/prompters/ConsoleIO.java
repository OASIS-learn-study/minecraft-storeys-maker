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

import java.io.Console;
import java.util.Objects;

public class ConsoleIO implements SimplePrompterIO {

    private final Console jico;

    public ConsoleIO() {
        this(System.console());
    }

    public ConsoleIO(Console console) {
        this.jico = Objects.requireNonNull(console, "No Console");
    }

    @Override
    public String readLine(String prompt) {
        return jico.readLine("%s ", prompt);
    }

    @Override
    public void writeLine(String info) {
        jico.format("%s\n", info);
        jico.flush();
    }
}