package ch.vorburger.minecraft.storeys.narrate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.Text;

public class Narrator {

    // TODO make this run async (and return Future?) in a work thread
    // (see https://docs.spongepowered.org/stable/en/plugin/scheduler.html)

    // TODO support narrating Text not String (but how to chop it up?)

    private static final Logger LOG = LoggerFactory.getLogger(Narrator.class);

    private final int maxLength = 10;
    private final int waitInMS = 2000;
    private final Splitter splitter = new Splitter();

    public void narrate(Entity entity, String text) {
        // Make sure name can always be seen, even if we are not closely look at entity
        entity.offer(Keys.CUSTOM_NAME_VISIBLE, true);

        for (String subText : splitter.split(maxLength, text)) {
            entity.offer(Keys.DISPLAY_NAME, Text.of(subText));
            LOG.info(subText);
            try {
                Thread.sleep(waitInMS);
            } catch (InterruptedException e) {
                return;
            }
        }

        // entity.offer(Keys.DISPLAY_NAME, Text.EMPTY);
    }

}
