package ch.vorburger.minecraft.storeys.model;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.scheduler.Task;

public class ActionWaitHelper {

    private final PluginInstance plugin;

    public ActionWaitHelper(PluginInstance plugin) {
        super();
        this.plugin = plugin;
    }

    public <T> CompletionStage<T> executeAndWait(int msToWaitAfterRunning, Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        try {
            T returnValue = callable.call();
            Task.builder()
                .async()
                .execute(() -> future.complete(returnValue))
                .delay(msToWaitAfterRunning, MILLISECONDS)
                .submit(plugin);

        } catch (Throwable throwable) {
            future.completeExceptionally(throwable);
        }
        return future;
    }

}
