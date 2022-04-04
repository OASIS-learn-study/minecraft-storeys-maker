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
package ch.vorburger.minecraft.storeys.util;

import static ch.vorburger.minecraft.storeys.util.MoreStrings.normalizeCRLF;
import static ch.vorburger.minecraft.storeys.util.MoreStrings.trimCRLF;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MoreStringsTest {

    @Test public void testNormalizeCRLF() {
        assertEquals("\nhello, \nworld\n", normalizeCRLF("\nhello, \nworld\n"));
        assertEquals("\nhello, \nworld\n", normalizeCRLF("\r\nhello, \r\nworld\r\n"));
    }

    @Test public void testTrimCRLF() {
        assertEquals("hello, \nworld", trimCRLF("\nhello, \nworld\n"));
        assertEquals("hello, \nworld", trimCRLF("\r\nhello, \r\nworld\n\n"));
    }
}
