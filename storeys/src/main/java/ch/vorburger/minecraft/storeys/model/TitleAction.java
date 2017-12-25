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
package ch.vorburger.minecraft.storeys.model;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.title.Title.Builder;

public class TitleAction extends TextAction<Void> {

    private static final int FADE_IN_MS = 50;
    private static final int FADE_IN_TICKS = (int) (FADE_IN_MS * 0.02);

    private static final int FADE_OUT_MS = 100;
    private static final int FADE_OUT_TICKS = (int) (FADE_OUT_MS * 0.02);

    private final ActionWaitHelper actionWaitHelper;

    private Text subtitleText;

    public TitleAction(PluginInstance plugin) {
        super();
        this.actionWaitHelper = new ActionWaitHelper(plugin);
    }

    public void setSubtitle(Text subtitleText) {
        this.subtitleText = subtitleText;
    }

    @Override
    public CompletionStage<Void> execute(ActionContext context) {
        Text bothTexts = getText().concat(subtitleText != null ? subtitleText : Text.EMPTY);
        int msToRead = context.getReadingSpeed().msToRead(bothTexts) + FADE_IN_MS + FADE_OUT_MS;

        return actionWaitHelper.executeAndWait(msToRead, () -> {
            CommandSource commandSource = context.getCommandSource();
            if (commandSource instanceof Viewer) {
                Viewer srcAsViewer = (Viewer) commandSource;

                Builder titleBuilder = Title.builder().fadeIn(FADE_IN_TICKS).stay((int) (msToRead * 0.02)).fadeOut(FADE_OUT_TICKS);
                titleBuilder.title(getText());
                if (subtitleText != null) {
                    titleBuilder.subtitle(subtitleText);
                }

                // TODO srcAsViewer.clearTitle(); ?
                srcAsViewer.sendTitle(titleBuilder.build());
                return null;
            } else {
                throw new ActionException("CommandSource is not a Viewer: " + commandSource.toString());
            }
        });
    }

}
