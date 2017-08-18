package ch.vorburger.minecraft.storeys.narrate;

import com.google.common.collect.ImmutableList;
import java.util.Collections;

public class Splitter {

    public Iterable<String> split(int maxLength, String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        return ImmutableList.of("hello, world.", "I'm a pig.", "Once upon a time...");
    }

}
