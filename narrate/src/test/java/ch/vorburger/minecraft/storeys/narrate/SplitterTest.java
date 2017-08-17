package ch.vorburger.minecraft.storeys.narrate;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class SplitterTest {

    @Test(expected = NullPointerException.class)
    public final void testNull() {
        new Splitter().split(123, null);
    }

    @Test
    public final void testEmpty() {
        assertThat(new Splitter().split(123, "")).isEmpty();
    }

    // TEST removing leading and trailing spaces in each segment

}
