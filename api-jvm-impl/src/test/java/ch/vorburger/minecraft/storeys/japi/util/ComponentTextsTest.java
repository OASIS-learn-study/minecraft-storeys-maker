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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.Test;

public class ComponentTextsTest {

    @Test public void plainTextStripsFormatting() {
        Component styled = Component.text("Piggy").color(NamedTextColor.GOLD);
        assertTrue(ComponentTexts.plainEquals(styled, "Piggy"));
        assertFalse(ComponentTexts.plainEquals(styled, "piggy"));
    }

    @Test public void plainEqualsMatchesUnstyledText() {
        assertTrue(ComponentTexts.plainEquals(Component.text("Alice"), "Alice"));
    }
}
