package ch.vorburger.minecraft.storeys.model;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import java.util.concurrent.CompletionStage;

public class MessageAction extends TextAction<Void> {

    private final ActionWaitHelper actionWaitHelper;

    public MessageAction(PluginInstance plugin) {
        super();
        this.actionWaitHelper = new ActionWaitHelper(plugin);
    }

    @Override
    public CompletionStage<Void> execute(ActionContext context) {
        return actionWaitHelper.executeAndWait(context.getReadingSpeed().msToRead(text), () -> {
            context.getCommandSource().sendMessage(text);
            return null;
        });
    }

}
