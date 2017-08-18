package ch.vorburger.minecraft.storeys.model;

import org.spongepowered.api.command.CommandSource;

public class MessageAction extends TextAction<Void> implements SynchronousAction<Void> {

    @Override
    public Void executeSynchronously(CommandSource src) throws ActionException {
        src.sendMessage(text);
        return null;
    }

}
