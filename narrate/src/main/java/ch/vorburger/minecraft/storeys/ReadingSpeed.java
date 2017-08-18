package ch.vorburger.minecraft.storeys;

import com.google.common.base.Preconditions;
import org.spongepowered.api.text.Text;

public class ReadingSpeed {

    static final int AVG_WORD_LENGTH = 5;
    private static final int AVG_CHILD_WORDS_PER_MINUTE = 100;

    private final int wpm;

    public ReadingSpeed() {
        this(AVG_CHILD_WORDS_PER_MINUTE);
    }

    public ReadingSpeed(int wordsPerMinute) {
        super();
        this.wpm = wordsPerMinute;
        Preconditions.checkArgument(wpm > 5, "Words per minute > 5, was: %s", wpm);
    }

    public ReadingSpeed by(double factor) {
        return new ReadingSpeed((int) (wpm * factor));
    }

    public int msToRead(Text text) {
        // This could be optimized, e.g. to skip long URLs,
        // which one does not typically fully read, like real text;
        // but at least for, we can just do this:
        return msToRead(text.toPlain());
    }

    public int msToRead(String text) {
        // TODO we could ditch all punctuation characters etc. here with a RegExp...
        return msToRead(text.length());
    }

    public int msToRead(int numberOfCharacters) {
        int approxWords = numberOfCharacters / AVG_WORD_LENGTH;
        // NB: We must force cast to double here, otherwise this won't work:
        double minutesForText = (double) approxWords / (double) wpm;
        int msForText = (int) (minutesForText * 60 * 1000);
        return msForText;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + wpm;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ReadingSpeed other = (ReadingSpeed) obj;
        if (wpm != other.wpm) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ReadingSpeed[wpm=" + wpm + "]";
    }

}
