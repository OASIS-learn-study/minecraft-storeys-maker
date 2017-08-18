package ch.vorburger.minecraft.storeys.model;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.title.Title.Builder;

public class TitleAction extends TextAction<Void> implements SynchronousAction<Void> {

    private Text subtitleText;

    public void setSubtitle(Text subtitleText) {
        this.subtitleText = subtitleText;
    }

    @Override
    public Void executeSynchronously(CommandSource src) throws ActionException {
        if (src instanceof Viewer) {
            Viewer srcAsViewer = (Viewer) src;

            Builder titleBuilder = Title.builder().fadeIn(60).stay(500).fadeOut(100);
            titleBuilder.title(text);
            if (subtitleText != null) {
                titleBuilder.subtitle(subtitleText);
            }

            // TODO srcAsViewer.clearTitle(); ?
            srcAsViewer.sendTitle(titleBuilder.build());
            return null;
        } else {
            throw new ActionException("CommandSource is not a Viewer: " + src.toString());
        }
    }

}
