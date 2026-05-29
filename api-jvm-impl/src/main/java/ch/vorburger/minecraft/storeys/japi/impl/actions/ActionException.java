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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ActionException extends Exception {

    private static final long serialVersionUID = 6261204063265579413L;
    private final TextComponent message;

    public ActionException(TextComponent message) {
        this.message = (message);
    }

    public ActionException(TextComponent message, Throwable throwable) {
        super(throwable);
        this.message = message;
    }

    public ActionException(String message) {
        this(Component.text(message).color(NamedTextColor.RED));
    }

    public ActionException(String message, Throwable throwable) {
        this(Component.text(message).color(NamedTextColor.RED), throwable);
    }

    public String getMessage() {
        TextComponent message = getText();
        return message == null ? null : message.content();
    }

    public TextComponent getText() {
        return this.message;
    }

}
