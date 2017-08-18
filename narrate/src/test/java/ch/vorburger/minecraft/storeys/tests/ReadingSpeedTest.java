package ch.vorburger.minecraft.storeys.tests;

import static com.google.common.truth.Truth.assertThat;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import org.junit.Test;

public class ReadingSpeedTest {

    @Test
    public final void empty() {
        assertThat(new ReadingSpeed().msToRead("")).isEqualTo(0);
    }

    @Test
    public final void example() {
        assertThat(new ReadingSpeed(200).msToRead("Lorem Ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")).isAtLeast(7100);
    }
}
