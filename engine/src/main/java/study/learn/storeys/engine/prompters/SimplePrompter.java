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
import java.util.Objects;
import java.util.function.Function;

import study.learn.storeys.engine.Prompt;
import study.learn.storeys.engine.Prompter;

public class SimplePrompter<T> implements Prompter<T> {

    private final SimplePrompterIO io;
    private final Object previousAnswer; // @Nullable

    private SimplePrompter(Object previousAnswer, SimplePrompterIO io) {
        this.previousAnswer = previousAnswer;
        this.io = Objects.requireNonNull(io, "No IO");
    }

    public SimplePrompter(SimplePrompterIO io) {
        this(null, io);
    }

    @Override
    public <X> Prompter<X> await(Prompt<X> prompt) throws IOException {
        Class<?> expectedType = prompt.getType();
        String promptText = prompt.getPrefix().getString();
        if (expectedType.equals(Void.TYPE)) {
            io.writeLine(promptText);
            // fall through (and thus ignore all following await)
            return new SimplePrompter<X>(null, io);
        }
        String answerString = io.readLine(promptText);
        if (answerString == null) {
            throw new IOException("EOF");
        }
        Object answer;
        if (expectedType.equals(String.class)) {
            answer = answerString;
        } else if (expectedType.equals(Integer.class)) {
            answer = Integer.parseInt(answerString);
        } else {
            throw new IllegalArgumentException("Unknown Prompt type: " + expectedType);
        }
        return new SimplePrompter<X>(answer, io);
    }

    @SuppressWarnings("unchecked") // because the (R) cast below should always work, the API doesn't let a user mismatch types
    public <X> Prompter<X> await(Function<T, Prompt<X>> function) throws IOException {
        if (previousAnswer == null) {
            // fall through, NOT throw new IllegalStateException("No previous answer");
            new SimplePrompter<X>(null, io);
        }
        return await(function.apply((T) previousAnswer));
    }
}
