package ch.vorburger.minecraft.storeys.model;

import ch.vorburger.minecraft.utils.Texts;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TextMessageException;

public class ActionException extends TextMessageException {

    private static final long serialVersionUID = 6261204063265579413L;

    public ActionException(Text message) {
        super(message);
    }

    public ActionException(Text message, Throwable throwable) {
        super(message, throwable);
    }

    public ActionException(String message) {
        this(Texts.inRed(message));
    }

    public ActionException(String message, Throwable throwable) {
        this(Texts.inRed(message), throwable);
    }

}
