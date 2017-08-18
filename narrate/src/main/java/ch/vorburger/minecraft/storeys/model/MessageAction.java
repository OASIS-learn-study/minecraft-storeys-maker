package ch.vorburger.minecraft.storeys.model;

public class MessageAction extends TextAction<Void> implements SynchronousAction<Void> {

    @Override
    public Void executeSynchronously(ActionContext context) throws ActionException {
        context.getCommandSource().sendMessage(text);
        return null;
    }

}
