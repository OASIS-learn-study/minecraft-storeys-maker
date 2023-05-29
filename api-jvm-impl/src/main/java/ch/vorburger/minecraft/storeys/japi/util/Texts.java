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
package ch.vorburger.minecraft.storeys.japi.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Utilities for {@link TextComponent}.
 *
 * @author Michael Vorburger.ch
 */
public final class Texts {

    // Copy/pasted from https://github.com/vorburger/ch.vorburger.minecraft.osgi/blob/master/ch.vorburger.minecraft.osgi.api/src/main/java/ch/vorburger/minecraft/utils/Texts.java 

    private Texts() {
    }

    public static TextComponent fromThrowable(String prefix, Throwable throwable) {
        // TODO have a Player isDeveloper flag (or Permission, probably..)
        // developers get to see the cause stack trace? ;) Noob do not.
        return Component.text().color(NamedTextColor.RED).content(prefix + throwable.getMessage()).build();
        // TODO add StackTrace here - with links being able to click on to jump into sources!!!
    }

    public static TextComponent inRed(String content) {
        return Component.text().color(NamedTextColor.RED).content(content).build();
    }
}
