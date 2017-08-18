package ch.vorburger.minecraft.storeys.tests;

import static com.google.common.truth.Truth.assertThat;

import ch.vorburger.minecraft.storeys.model.parser.StoryParser;
import ch.vorburger.minecraft.storeys.model.parser.ClassLoaderResourceStoryRepository;
import ch.vorburger.minecraft.storeys.model.parser.SyntaxErrorException;
import java.io.IOException;
import org.junit.Test;

public class ActionParserTest {

    private final StoryParser parser = new StoryParser(null);

    @Test public void helloStory() throws IOException, SyntaxErrorException {
        assertThat(
            parser.parse(new ClassLoaderResourceStoryRepository().getStoryScript("hello")).getActionsList())
            .hasSize(9);
    }

    @Test public void empty() throws SyntaxErrorException {
        assertThat(parser.parse("").getActionsList()).isEmpty();;
    }

    @Test public void blanks() throws SyntaxErrorException {
        assertThat(parser.parse("   \n \r\n  ").getActionsList()).isEmpty();;
    }

    @Test public void comments() throws SyntaxErrorException {
        assertThat(parser.parse("# @Entity ... \n // Comment \r\n  ").getActionsList()).isEmpty();;
    }

    @Test public void titles() throws SyntaxErrorException {
        assertThat(parser.parse(
                "= Once upon a time..\n== There was a pig.\n").getActionsList())
            .hasSize(1);
    }

}
