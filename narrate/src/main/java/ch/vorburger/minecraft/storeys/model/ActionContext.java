package ch.vorburger.minecraft.storeys.model;

import ch.vorburger.minecraft.storeys.ReadingSpeed;
import org.spongepowered.api.command.CommandSource;

public final class ActionContext {

    private final CommandSource commandSource;
    private final ReadingSpeed readingSpeed;

    public ActionContext(CommandSource commandSource, ReadingSpeed readingSpeed) {
        super();
        this.commandSource = commandSource;
        this.readingSpeed = readingSpeed;
    }

    public CommandSource getCommandSource() {
        return commandSource;
    }

    public ReadingSpeed getReadingSpeed() {
        return readingSpeed;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (commandSource == null ? 0 : commandSource.hashCode());
        result = prime * result + (readingSpeed == null ? 0 : readingSpeed.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ActionContext other = (ActionContext) obj;
        if (commandSource == null) {
            if (other.commandSource != null) {
                return false;
            }
        } else if (!commandSource.equals(other.commandSource)) {
            return false;
        }
        if (readingSpeed == null) {
            if (other.readingSpeed != null) {
                return false;
            }
        } else if (!readingSpeed.equals(other.readingSpeed)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ActionContext[commandSource=" + commandSource + ", readingSpeed=" + readingSpeed + "]";
    }

}
