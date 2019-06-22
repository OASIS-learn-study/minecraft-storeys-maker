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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class Prompt<T> {

    abstract public Text getPrefix();
    abstract public List<String> getChoiceIDs();
    abstract public List<Text> getChoiceLabels();
    abstract public Class<?> getType();

    static Prompt<String> aString(String prefix) {
        return new Impl<>(prefix, String.class);
    }

    static Prompt<Integer> anInt(String prefix) {
        return new Impl<>(prefix, Integer.class);
    }

	static Prompt<String> aChoice(String prefix, List<String> choiceIDAndLabels) {
        return new Impl<>(prefix, choiceIDAndLabels, List.class);
	}

    static Prompt<Void> bye(String prefix) {
        return new Impl<>(prefix, Void.TYPE);
    }

    private static class Impl<T> extends Prompt<T> {
        private final Text prefix;
		private List<String> choiceIDs;
		private List<Text> choiceLabels;
        private final Class<?> type;

        Impl(String prefix, Class<?> type) {
            this(prefix, Collections.emptyList(), type);
        }

        Impl(String prefix, List<String> choiceIDAndLabels, Class<?> type) {
            this.prefix = Text.ofString(prefix);
            this.choiceIDs = new ArrayList<>();
            this.choiceLabels = new ArrayList<>();
            this.type = type;

            Iterator<String> choiceIDAndLabel = choiceIDAndLabels.iterator();
            while (choiceIDAndLabel.hasNext()) {
                this.choiceIDs.add(choiceIDAndLabel.next());
                this.choiceLabels.add(Text.ofString(choiceIDAndLabel.next()));
            }
        }
/*
        Impl(Text prefix, List<String> choiceIDs, List<Text> choiceLabels, Class<?> type) {
            this.prefix = prefix;
            this.choiceIDs = choiceIDs;
            this.choiceLabels = choiceLabels;
            this.type = type;
        }
*/
		@Override
        public Text getPrefix() {
            return this.prefix;
        }

		@Override
		public List<String> getChoiceIDs() {
			return choiceIDs;
		}

		@Override
		public List<Text> getChoiceLabels() {
			return choiceLabels;
		}

		@Override
		public Class<?> getType() {
			return this.type;
		}
    }
}
