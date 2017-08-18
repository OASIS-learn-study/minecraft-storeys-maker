package ch.vorburger.minecraft.storeys.model;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.title.Title.Builder;

public class TitleAction extends TextAction<Void> {

    private final ActionWaitHelper actionWaitHelper;

    private Text subtitleText;

    public TitleAction(PluginInstance plugin) {
        super();
        this.actionWaitHelper = new ActionWaitHelper(plugin);
    }

    public void setSubtitle(Text subtitleText) {
        this.subtitleText = subtitleText;
    }

    @Override
    public CompletionStage<Void> execute(CommandSource src) {
        return actionWaitHelper.executeAndWait(2000, () -> {
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
        });
    }

}
