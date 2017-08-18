package ch.vorburger.minecraft.storeys.model;

import ch.vorburger.minecraft.storeys.narrate.Narrator;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Entity;

public class NarrateAction extends TextAction<Void> {

    private final Narrator narrator;

    private Entity entity;

    public NarrateAction(Narrator narrator) {
        this.narrator = narrator;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public CompletionStage<Void> execute(CommandSource src) {
        return narrator.narrate(entity, text.toPlain());
    }

}
