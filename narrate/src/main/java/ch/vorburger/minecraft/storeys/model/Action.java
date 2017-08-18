package ch.vorburger.minecraft.storeys.model;

import java.util.concurrent.CompletionStage;
import org.spongepowered.api.command.CommandSource;

public interface Action<T> {

    // TODO throws ActionException or return list of ActionValidationViolation-s?
    // void validate();

    CompletionStage<T> execute(CommandSource src);

    // default <T> T requireNonNull(T obj, String propertyName) {
    // default void checkArgument(boolean test, String propertyValidationErrorMessage) {
}
