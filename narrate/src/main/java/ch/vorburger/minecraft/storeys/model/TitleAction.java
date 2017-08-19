package ch.vorburger.minecraft.storeys.model;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.title.Title.Builder;

public class TitleAction extends TextAction<Void> {

    private static final int FADE_IN_MS = 50;
    private static final int FADE_IN_TICKS = (int) (FADE_IN_MS * 0.02);

    private static final int FADE_OUT_MS = 100;
    private static final int FADE_OUT_TICKS = (int) (FADE_OUT_MS * 0.02);

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
    public CompletionStage<Void> execute(ActionContext context) {
        Text bothTexts = text.concat(subtitleText != null ? subtitleText : Text.EMPTY);
        int msToRead = context.getReadingSpeed().msToRead(bothTexts) + FADE_IN_MS + FADE_OUT_MS;

        return actionWaitHelper.executeAndWait(msToRead, () -> {
            CommandSource commandSource = context.getCommandSource();
            if (commandSource instanceof Viewer) {
                Viewer srcAsViewer = (Viewer) commandSource;

                Builder titleBuilder = Title.builder().fadeIn(FADE_IN_TICKS).stay((int) (msToRead * 0.02)).fadeOut(FADE_OUT_TICKS);
                titleBuilder.title(text);
                if (subtitleText != null) {
                    titleBuilder.subtitle(subtitleText);
                }

                // TODO srcAsViewer.clearTitle(); ?
                srcAsViewer.sendTitle(titleBuilder.build());
                return null;
            } else {
                throw new ActionException("CommandSource is not a Viewer: " + commandSource.toString());
            }
        });
    }

}
