/**
 * ch.vorburger.minecraft.storeys
 *
 * Copyright (C) 2016 - 2017 Michael Vorburger.ch <mike@vorburger.ch>
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
package ch.vorburger.minecraft.storeys;

import ch.vorburger.minecraft.storeys.util.MoreStrings;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextSplitter {

    /**
     * Split text into segments of ideally maxLength.
     * Splitting is by occurrence space, and if some words are longer than maxLength,
     * then some segments may contain single words which are longer than maxLenght.
     */
    public Iterable<String> split(int maxLength, String text) {
        Preconditions.checkArgument(maxLength > 0, "maxLength < 1");
        Preconditions.checkNotNull(text, "text == null");

        text = MoreStrings.normalizeCRLF(text).trim();
        if (text.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> segments = new ArrayList<>(text.length() / ReadingSpeed.AVG_WORD_LENGTH / maxLength);

        text = text.trim();
        int start = 0, end = 0, lastSpace = -1;
        while (end < text.length()) {
            if (text.charAt(end) == ' ') {
                lastSpace = end;
            }
            if (text.charAt(end) == '\n') {
                add(segments, text.substring(start, end));
                start = end + 1;
                lastSpace = -1;
            }
            if (end - start > maxLength) {
                if (lastSpace > start) {
                    add(segments, text.substring(start, lastSpace));
                    start = lastSpace + 1;
                }
                lastSpace = -1;
            }
            ++end;
        }
        // We still have to add the last segment now
        add(segments, text.substring(start, end));

        return segments;
    }

    private void add(List<String> segments, String subStringToAdd) {
        String trimmed = subStringToAdd.trim();
        // skip segments of only \n or that are completely empty
        if (!trimmed.equals("\n") && !trimmed.isEmpty()) {
            segments.add(trimmed);
        }
    }

}
