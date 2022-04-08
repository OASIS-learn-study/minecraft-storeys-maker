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
package ch.vorburger.minecraft.storeys.tests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.iterableWithSize;

import ch.vorburger.minecraft.storeys.TextSplitter;
import org.junit.Test;

public class SplitterTest {

    @Test(expected = NullPointerException.class) public void nulled() {
        new TextSplitter().split(123, null);
    }

    @Test public void simplest() {
        assertThat(new TextSplitter().split(123, "hello"), contains("hello"));
    }

    @Test public void twoInOne() {
        assertThat(new TextSplitter().split(123, "hello world"), contains("hello world"));
        assertThat(new TextSplitter().split(11, "hello world"), contains("hello world"));
    }

    @Test public void twoInTwo() {
        assertThat(new TextSplitter().split(5, "hello world"), contains("hello", "world"));
        assertThat(new TextSplitter().split(6, "hello world"), contains("hello", "world"));
        assertThat(new TextSplitter().split(7, "hello world"), contains("hello", "world"));
    }

    @Test public void threeInTwo() {
        assertThat(new TextSplitter().split(7, "hello, wor ld"), contains("hello,", "wor ld"));
    }

    @Test public void tooSmall() {
        assertThat(new TextSplitter().split(1, "hello"), contains("hello"));
        assertThat(new TextSplitter().split(1, "hello world"), contains("hello", "world"));
    }

    @Test public void splitAtCR() {
        assertThat(new TextSplitter().split(123, "hello \n world"), contains("hello", "world"));
        assertThat(new TextSplitter().split(123, "hello\nworld"), contains("hello", "world"));
        assertThat(new TextSplitter().split(1, "hello there\nworld"), contains("hello", "there", "world"));
    }

    @Test public void multipleCR() {
        assertThat(new TextSplitter().split(123, "hello\n\nworld"), contains("hello", "world"));
        assertThat(new TextSplitter().split(123, "hello\n  \nworld"), contains("hello", "world"));
    }

    @Test public void emptyTest() {
        assertThat(new TextSplitter().split(123, ""), iterableWithSize(0));
    }

    @Test public void oneChar() {
        assertThat(new TextSplitter().split(123, "a"), contains("a"));
    }

    @Test public void leadingTrailingSpaces() {
        assertThat(new TextSplitter().split(123, " "), iterableWithSize(0));
        assertThat(new TextSplitter().split(123, "  "), iterableWithSize(0));
        assertThat(new TextSplitter().split(123, " hello "), contains("hello"));
        assertThat(new TextSplitter().split(5, " hello   world  "), contains("hello", "world"));
        assertThat(new TextSplitter().split(13, " hello   world  "), contains("hello   world"));
    }

    @Test public void leadingTrailingCR() {
        assertThat(new TextSplitter().split(123, "\n"), iterableWithSize(0));
        assertThat(new TextSplitter().split(123, "\n\n"), iterableWithSize(0));
        assertThat(new TextSplitter().split(123, "\nhello\n"), contains("hello"));
    }

    @Test public void windowsCR_LF() {
        assertThat(new TextSplitter().split(123, "\r\n"), iterableWithSize(0));
        assertThat(new TextSplitter().split(123, "\r\nhello\r\n"), contains("hello"));
        assertThat(new TextSplitter().split(123, "hello\r\nworld"), contains("hello", "world"));
    }

    @Test(expected = IllegalArgumentException.class) public void negative() {
        new TextSplitter().split(-1, "hello");
    }

    @Test(expected = IllegalArgumentException.class) public void zero() {
        new TextSplitter().split(0, "hello");
    }

}
