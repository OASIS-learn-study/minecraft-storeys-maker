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

import ch.vorburger.minecraft.utils.Texts;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TextMessageException;

public class ActionException extends TextMessageException {

    private static final long serialVersionUID = 6261204063265579413L;

    public ActionException(Text message) {
        super(message);
    }

    public ActionException(Text message, Throwable throwable) {
        super(message, throwable);
    }

    public ActionException(String message) {
        this(Texts.inRed(message));
    }

    public ActionException(String message, Throwable throwable) {
        this(Texts.inRed(message), throwable);
    }

}
