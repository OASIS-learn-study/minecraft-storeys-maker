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
package ch.vorburger.minecraft.storeys.japi.impl.actions;

import ch.vorburger.minecraft.storeys.japi.ActionContext;
import ch.vorburger.minecraft.storeys.japi.ReadingSpeed;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandCause;

public final class ActionContextImpl implements ActionContext {

    private final Audience commandCause;
    private final ReadingSpeed readingSpeed;

    public ActionContextImpl(Audience commandCause, ReadingSpeed readingSpeed) {
        super();
        this.commandCause = commandCause;
        this.readingSpeed = readingSpeed;
    }

    public Audience getCommandCause() {
        return commandCause;
    }

    public ReadingSpeed getReadingSpeed() {
        return readingSpeed;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (commandCause == null ? 0 : commandCause.hashCode());
        result = (prime * result) + (readingSpeed == null ? 0 : readingSpeed.hashCode());
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
        ActionContextImpl other = (ActionContextImpl) obj;
        if (commandCause == null) {
            if (other.commandCause != null) {
                return false;
            }
        } else if (!commandCause.equals(other.commandCause)) {
            return false;
        }
        if (readingSpeed == null) {
            return other.readingSpeed == null;
        } else
            return readingSpeed.equals(other.readingSpeed);
    }

    @Override public String toString() {
        return "ActionContext[commandSource=" + commandCause + ", readingSpeed=" + readingSpeed + "]";
    }

}
