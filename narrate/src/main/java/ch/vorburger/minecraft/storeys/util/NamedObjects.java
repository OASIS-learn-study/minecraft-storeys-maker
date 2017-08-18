package ch.vorburger.minecraft.storeys.util;

import static org.spongepowered.api.data.key.Keys.DISPLAY_NAME;

import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.World;

public class NamedObjects {

    // TODO implement a "/name xyz" command which "names" the next clicked entity!

    // TODO instead of using name tags, just have a properties files saving name -> UUID?

    // TODO beware, world.getEntities (getEntity(uuid)) will only find in loaded chunks.. how to find anywhere?

    // TODO could names be saved in a custom DataHolder DataView Property ?
    // see https://docs.spongepowered.org/stable/en/plugin/data/custom/datamanipulators.html

    private static final Logger LOG = LoggerFactory.getLogger(NamedObjects.class);

    public Optional<Entity> getEntity(World world, String entityName) {
        Collection<Entity> entities = world.getEntities(entity -> entity.get(DISPLAY_NAME).filter(name -> entityName.equals(name.toPlain())).isPresent());
        // entities.removeIf(entity -> entity instanceof Player);
        if (entities.isEmpty()) {
            return Optional.empty();
        } else {
            if (entities.size() > 1) {
                LOG.warn("World {} has more than 1 entity with display name {} (return first one, arbitratry!)", world.getName(), entityName);
                entities.stream().forEach(entity -> LOG.info(entity.toString()));
            }
            return Optional.of(entities.iterator().next());
        }
    }

}
