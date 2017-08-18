package ch.vorburger.minecraft.storeys.model;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface SynchronousAction<T> extends Action<T> {

    T executeSynchronously(ActionContext context) throws ActionException;

    @Override
    default CompletionStage<T> execute(ActionContext context) {
        try {
            return CompletableFuture.completedFuture(executeSynchronously(context));
        } catch (Throwable throwable) {
            CompletableFuture<T> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(throwable);
            return failedFuture;
        }
    }
}
