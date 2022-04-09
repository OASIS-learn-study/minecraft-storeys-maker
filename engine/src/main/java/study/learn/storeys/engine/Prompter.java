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
import study.learn.storeys.engine.util.CheckedFunction;

public interface Prompter<T> {

    <X> Prompter<X> await(Prompt<X> prompt) throws IOException;

    // TODO split this into separate interface, which extends this, to avoid initial await when there is no value, yet
    <X> Prompter<X> await(CheckedFunction<T, Prompt<X>, IOException> function) throws IOException;
}
