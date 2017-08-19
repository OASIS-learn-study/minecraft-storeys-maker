package ch.vorburger.minecraft.storeys.model;

import ch.vorburger.minecraft.storeys.Narrator;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;

public class NarrateAction extends TextAction<Void> {

    private final Narrator narrator;

    private String entityName;

    public NarrateAction(Narrator narrator) {
        this.narrator = narrator;
    }

    public void setEntity(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public CompletionStage<Void> execute(ActionContext context) {
        Locatable locatable = (Locatable) context.getCommandSource();
        World world = locatable.getWorld();

        return narrator.narrate(world, entityName, text.toPlain(), context.getReadingSpeed());
    }

}
