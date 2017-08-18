package ch.vorburger.minecraft.storeys.model.parser;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;

import com.google.common.io.Resources;
import java.io.IOException;

public class ClassLoaderResourceStoryRepository implements StoryRepository {

    @Override
    public String getStoryScript(String storyName) throws IOException {
        return Resources.toString(getResource(ClassLoaderResourceStoryRepository.class, "/" + storyName + ".story"), UTF_8);
    }

}
