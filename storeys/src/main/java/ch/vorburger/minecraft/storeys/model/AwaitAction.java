package ch.vorburger.minecraft.storeys.model;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import com.google.common.base.Preconditions;
import java.util.concurrent.CompletionStage;

public class AwaitAction implements Action<Void> {

    private final ActionWaitHelper actionWaitHelper;
    private int msToWait;

    public AwaitAction(PluginInstance plugin) {
        this.actionWaitHelper = new ActionWaitHelper(plugin);
    }

    public AwaitAction setMsToWait(int msToWait) {
        Preconditions.checkArgument(msToWait > 100, "msToWait > 100");
        this.msToWait = msToWait;
        return this;
    }

    @Override
    public CompletionStage<Void> execute(ActionContext context) {
        return actionWaitHelper.executeAndWait(msToWait, () -> null);
    }
}
