package ch.vorburger.minecraft.storeys.model.parser;

import java.io.IOException;

public interface StoryRepository {

    String getStoryScript(String storyName) throws IOException;

    // TODO List<String> getStoryNames(String prefix)

}
