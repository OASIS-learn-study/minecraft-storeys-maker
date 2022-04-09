/*
 * ch.vorburger.minecraft.storeys
 *
 * Copyright (C) 2016 - 2018 Michael Vorburger.ch <mike@vorburger.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (commandSource == null ? 0 : commandSource.hashCode());
        result = prime * result + (readingSpeed == null ? 0 : readingSpeed.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
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

    @Override public String toString() {
        return "ActionContext[commandSource=" + commandSource + ", readingSpeed=" + readingSpeed + "]";
    }

}
