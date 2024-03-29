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

import ch.vorburger.minecraft.storeys.japi.Action;
import ch.vorburger.minecraft.storeys.japi.ActionContext;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ActionPlayer {

    public CompletionStage<?> play(ActionContext context, List<Action<?>> actions) {
        // TODO This must most probably run in an async (!) Task, similar to Narrator - or does it not (because Actions are
        // already async) ?
        CompletionStage<?> previousCompletionStage = null;
        for (Action<?> action : actions) {
            if (previousCompletionStage != null) {
                previousCompletionStage = previousCompletionStage.thenCompose(lastResult -> action.execute(context));
            } else {
                previousCompletionStage = action.execute(context);
            }
        }
        return previousCompletionStage;
    }
}
