package ch.vorburger.minecraft.storeys.model;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.title.Title.Builder;

public class TitleAction extends TextAction<Void> implements SynchronousAction<Void> {

    private boolean isSubTitle = false;

    public void setSubTitle(boolean isSubTitle) {
        this.isSubTitle = isSubTitle;
    }

    @Override
    public Void executeSynchronously(CommandSource src) throws ActionException {
        if (src instanceof Viewer) {
            Viewer srcAsViewer = (Viewer) src;

            Builder titleBuilder = Title.builder().fadeIn(60).stay(500).fadeOut(100);
            if (!isSubTitle) {
                titleBuilder.title(text);
            } else {
                titleBuilder.subtitle(text);
            }

            // TODO srcAsViewer.clearTitle(); ?
            srcAsViewer.sendTitle(titleBuilder.build());
            return null;
        } else {
            throw new ActionException("CommandSource is not a Viewer: " + src.toString());
        }
    }

}
