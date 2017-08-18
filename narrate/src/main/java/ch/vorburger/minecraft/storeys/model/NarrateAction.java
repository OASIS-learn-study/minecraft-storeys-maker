package ch.vorburger.minecraft.storeys.model;

import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.util.NamedObjects;
import com.flowpowered.math.vector.Vector3d;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;

public class NarrateAction extends TextAction<Void> {

    private final NamedObjects namedObjects = new NamedObjects();
    private final Narrator narrator;

    private String entityName;

    public NarrateAction(Narrator narrator) {
        this.narrator = narrator;
    }

    public void setEntity(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public CompletionStage<Void> execute(CommandSource src) {
        Locatable locatable = (Locatable) src;
        World world = locatable.getWorld();
        Entity entity = namedObjects.getEntity(world , entityName).orElseThrow(() -> new IllegalArgumentException("No entity named: " + entityName));

        // Duh - STOP MOVING! ;)
        // TODO later probably remove this again? Have it in the script with a command.. how?
        entity.setVelocity(new Vector3d(0.0, 0.0, 0.0));

        return narrator.narrate(entity, text.toPlain());
    }

}
