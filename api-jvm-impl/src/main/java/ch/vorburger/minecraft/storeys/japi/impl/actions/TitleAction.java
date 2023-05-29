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

import static ch.vorburger.minecraft.storeys.japi.util.MoreStrings.trimCRLF;

import ch.vorburger.minecraft.storeys.japi.Action;
import ch.vorburger.minecraft.storeys.japi.ActionContext;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.effect.Viewer;

public class TitleAction extends TextAction<Void> {

    private static final int FADE_IN_MS = 50;
    private static final int FADE_IN_TICKS = (int) (FADE_IN_MS * 0.02);

    private static final int FADE_OUT_MS = 100;
    private static final int FADE_OUT_TICKS = (int) (FADE_OUT_MS * 0.02);

    private final ActionWaitHelper actionWaitHelper;

    private TextComponent subtitleText;

    @Inject public TitleAction(ActionWaitHelper helper) {
        this.actionWaitHelper = helper;
    }

    @Override public void setParameter(String param) {
        String[] parts = param.split("==");
        if (parts.length == 1) {
            super.setParameter(trimCRLF(param));
        } else {
            super.setParameter(trimCRLF(parts[0]));
            subtitleText = Component.text(trimCRLF(parts[1].trim()));
        }
    }

    @Override public CompletionStage<Void> execute(ActionContext context) {
        TextComponent bothTexts = getText().append(subtitleText != null ? subtitleText : Component.empty());
        int msToRead = context.getReadingSpeed().msToRead(bothTexts) + FADE_IN_MS + FADE_OUT_MS;

        return actionWaitHelper.executeAndWait(msToRead, () -> {
            final Audience commandCause = context.getCommandCause();
            if (commandCause instanceof Viewer) {
                Viewer srcAsViewer = (Viewer) commandCause;

                final Title.Times times = Title.Times.of(Duration.ofMillis(FADE_IN_TICKS), Duration.ofMillis((int) (msToRead * 0.02)),
                        Duration.ofMillis(FADE_OUT_TICKS));
                Title titleBuilder = Title.title(getText(), subtitleText != null ? subtitleText : Component.empty(), times);

                // TODO srcAsViewer.clearTitle(); ?
                srcAsViewer.showTitle(titleBuilder);
                return null;
            } else {
                throw new ActionException("CommandSource is not a Viewer: " + commandCause.toString());
            }
        });
    }

    @Override public boolean add(Action<?> action) {
        if (action instanceof TitleAction) {
            this.subtitleText = ((TitleAction) action).getText();
            return true;
        }

        return false;
    }

    @Override public String toString() {
        return super.toString() + "==" + (subtitleText != null ? subtitleText.toString() : "null");
    }
}
