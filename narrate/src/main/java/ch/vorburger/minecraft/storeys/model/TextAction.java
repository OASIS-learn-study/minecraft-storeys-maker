package ch.vorburger.minecraft.storeys.model;

import org.spongepowered.api.text.Text;

public abstract class TextAction<T> implements Action<T> {

    protected Text text;

    protected TextAction() {
    }

    public Text getText() {
        return text;
    }

    public TextAction<T> setText(Text text) {
        this.text = text;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + text.toString();
    }

}
