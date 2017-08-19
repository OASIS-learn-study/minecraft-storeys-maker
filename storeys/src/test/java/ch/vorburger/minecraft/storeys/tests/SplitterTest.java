package ch.vorburger.minecraft.storeys.tests;

import static com.google.common.truth.Truth.assertThat;

import ch.vorburger.minecraft.storeys.TextSplitter;
import org.junit.Test;

public class SplitterTest {

    @Test(expected = NullPointerException.class) public void nulled() {
        new TextSplitter().split(123, null);
    }

    @Test public void simplest() {
        assertThat(new TextSplitter().split(123, "hello")).containsExactly("hello");
    }

    @Test public void twoInOne() {
        assertThat(new TextSplitter().split(123, "hello world")).containsExactly("hello world");
        assertThat(new TextSplitter().split( 11, "hello world")).containsExactly("hello world");
    }

    @Test public void twoInTwo() {
        assertThat(new TextSplitter().split(5, "hello world")).containsExactly("hello", "world").inOrder();
        assertThat(new TextSplitter().split(6, "hello world")).containsExactly("hello", "world").inOrder();
        assertThat(new TextSplitter().split(7, "hello world")).containsExactly("hello", "world").inOrder();
    }

    @Test public void threeInTwo() {
        assertThat(new TextSplitter().split(7, "hello, wor ld")).containsExactly("hello,", "wor ld").inOrder();
    }

    @Test public void tooSmall() {
        assertThat(new TextSplitter().split(1, "hello")).containsExactly("hello");
        assertThat(new TextSplitter().split(1, "hello world")).containsExactly("hello", "world").inOrder();
    }

    @Test public void splitAtCR() {
        assertThat(new TextSplitter().split(123, "hello \n world")).containsExactly("hello", "world").inOrder();
        assertThat(new TextSplitter().split(123, "hello\nworld")).containsExactly("hello", "world").inOrder();
        assertThat(new TextSplitter().split(1, "hello there\nworld")).containsExactly("hello", "there", "world").inOrder();
    }

    @Test public void multipleCR() {
        assertThat(new TextSplitter().split(123, "hello\n\nworld")).containsExactly("hello", "world").inOrder();
        assertThat(new TextSplitter().split(123, "hello\n  \nworld")).containsExactly("hello", "world").inOrder();
    }

    @Test public void empty() {
        assertThat(new TextSplitter().split(123, "")).isEmpty();
    }

    @Test public void oneChar() {
        assertThat(new TextSplitter().split(123, "a")).containsExactly("a");
    }

    @Test public void leadingTrailingSpaces() {
        assertThat(new TextSplitter().split(123, " ")).isEmpty();
        assertThat(new TextSplitter().split(123, "  ")).isEmpty();
        assertThat(new TextSplitter().split(123, " hello ")).containsExactly("hello");
        assertThat(new TextSplitter().split( 5, " hello   world  ")).containsExactly("hello", "world").inOrder();
        assertThat(new TextSplitter().split(13, " hello   world  ")).containsExactly("hello   world");
    }

    @Test public void leadingTrailingCR() {
        assertThat(new TextSplitter().split(123, "\n")).isEmpty();
        assertThat(new TextSplitter().split(123, "\n\n")).isEmpty();
        assertThat(new TextSplitter().split(123, "\nhello\n")).containsExactly("hello");
    }

    @Test public void WindowsCR_LF() {
        assertThat(new TextSplitter().split(123, "\r\n")).isEmpty();
        assertThat(new TextSplitter().split(123, "\r\nhello\r\n")).containsExactly("hello");
        assertThat(new TextSplitter().split(123, "hello\r\nworld")).containsExactly("hello", "world").inOrder();
    }

    @Test(expected = IllegalArgumentException.class) public void negative() {
        assertThat(new TextSplitter().split(-1, "hello"));
    }

    @Test(expected = IllegalArgumentException.class) public void zero() {
        assertThat(new TextSplitter().split(0, "hello"));
    }

}
