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
package ch.vorburger.minecraft.storeys.japi.impl.actions;

import org.spongepowered.api.text.Text;

public class ReadingSpeed {

    static final int AVG_WORD_LENGTH = 5;
    private static final int AVG_CHILD_WORDS_PER_MINUTE = 100;

    private final int wpm;

    public ReadingSpeed() {
        this(AVG_CHILD_WORDS_PER_MINUTE);
    }

    public ReadingSpeed(int wordsPerMinute) {
        super();
        this.wpm = wordsPerMinute;
        // TODO Figure out NoSuchMethodError: com.google.common.base.Preconditions.checkArgument(ZLjava/lang/String;I)V
        // Preconditions.checkArgument(wpm > 5, "Words per minute > 5, was: %s", wpm);
        // Preconditions.checkArgument(wpm < 500, "Words per minute < 500, was: %s", wpm);
    }

    public ReadingSpeed by(double factor) {
        return new ReadingSpeed((int) (wpm * factor));
    }

    public int msToRead(Text text) {
        // This could be optimized, e.g. to skip long URLs,
        // which one does not typically fully read, like real text;
        // but at least for, we can just do this:
        return msToRead(text.toPlain());
    }

    public int msToRead(String text) {
        // TODO we could ditch all punctuation characters etc. here with a RegExp...
        return msToRead(text.length());
    }

    public int msToRead(int numberOfCharacters) {
        int approxWords = numberOfCharacters / AVG_WORD_LENGTH;
        // NB: We must force cast to double here, otherwise this won't work:
        double minutesForText = (double) approxWords / (double) wpm;
        int msForText = (int) (minutesForText * 60 * 1000);
        return msForText;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + wpm;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ReadingSpeed other = (ReadingSpeed) obj;
        if (wpm != other.wpm) {
            return false;
        }
        return true;
    }

    @Override public String toString() {
        return "ReadingSpeed[wpm=" + wpm + "]";
    }

}
