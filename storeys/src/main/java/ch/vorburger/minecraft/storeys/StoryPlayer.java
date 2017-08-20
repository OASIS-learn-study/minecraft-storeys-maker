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

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.ActionContext;
import ch.vorburger.minecraft.storeys.model.Story;
import java.util.concurrent.CompletionStage;

public class StoryPlayer {

    private final PluginInstance plugin;

    public StoryPlayer(PluginInstance plugin) {
        super();
        this.plugin = plugin;
    }

    public CompletionStage<?> play(ActionContext context, Story story) {
        // TODO This must most probably run in an async (!) Task, similar to Narrator - or does it not (because Actions are already async) ?
        CompletionStage<?> previousCompletionStage = null;
        for (Action<?> action : story.getActionsList()) {
            if (previousCompletionStage != null) {
                previousCompletionStage = previousCompletionStage.thenCompose(lastResult -> action.execute(context));
            } else {
                previousCompletionStage = action.execute(context);
            }
        }
        return previousCompletionStage;
    }

}
