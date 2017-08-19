package ch.vorburger.minecraft.storeys.model;

import java.util.List;

public class Story {

    private final List<Action<?>> actionsList;

    public Story(List<Action<?>> actionsList) {
        super();
        this.actionsList = actionsList;
    }

    public List<Action<?>> getActionsList() {
        return actionsList;
    }
}
