package ch.vorburger.minecraft.storeys.util;

import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

public class NamedObjects {

    // TODO implement a "/name xyz" command which "names" the next clicked entity!

    // TODO instead of using name tags, just have a properties files saving name -> UUID?

    // TODO beware, world.getEntities (getEntity(uuid)) will only find in loaded chunks.. how to find anywhere?

    // TODO could names be saved in a custom DataHolder DataView Property ?
    // see https://docs.spongepowered.org/stable/en/plugin/data/custom/datamanipulators.html

    private static final Logger LOG = LoggerFactory.getLogger(NamedObjects.class);

    public Optional<Entity> getEntity(World world, String entityName) {
        Collection<Entity> entities = world.getEntities(entity -> entity.get(Keys.DISPLAY_NAME).isPresent());
        entities.removeIf(entity -> entity instanceof Player);
        if (entities.isEmpty()) {
            return Optional.empty();
        } else {
            if (entities.size() > 1) {
                LOG.warn("World has more than 1 entity with this display name, return (arbitratry!) first one: " + entityName);
                entities.stream().forEach(entity -> LOG.info(entity.toString()));
            }
            return Optional.of(entities.iterator().next());
        }
    }

}
