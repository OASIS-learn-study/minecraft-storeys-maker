package ch.vorburger.minecraft.storeys.model;

import org.spongepowered.api.text.Text;

public abstract class TextAction<T> implements Action<T> {

    protected Text text;

    protected TextAction() {
    }

    public void setText(Text text) {
        this.text = text;
    }
}
