package ch.vorburger.minecraft.storeys.model.parser;

import static com.google.common.base.Charsets.UTF_8;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;

public class FileStoryRepository implements StoryRepository {

    private final File rootDirectory;

    public FileStoryRepository(File rootDirectory) {
        super();
        this.rootDirectory = rootDirectory;
    }

    @Override
    public String getStoryScript(String storyName) throws IOException {
        return Files.toString(new File(rootDirectory, storyName + ".story"), UTF_8);
    }

}
