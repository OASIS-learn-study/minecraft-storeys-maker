package ch.vorburger.minecraft.storeys.model;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.command.CommandSource;

public interface SynchronousAction<T> extends Action<T> {

    T executeSynchronously(CommandSource src) throws ActionException;

    @Override
    default CompletionStage<T> execute(CommandSource src) {
        try {
            return CompletableFuture.completedFuture(executeSynchronously(src));
        } catch (Throwable throwable) {
            CompletableFuture<T> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(throwable);
            return failedFuture;
        }
    }
}
