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

import static java.util.Objects.requireNonNull;

import ch.vorburger.minecraft.storeys.Narrator;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;

public class NarrateAction extends TextAction<Void> {
    private static final Pattern ENTITY_NAME_PATTERN = Pattern.compile("@([a-zA-Z]*)\\s");
    private final Narrator narrator;

    private String entityName;

    @Inject public NarrateAction(Narrator narrator) {
        this.narrator = narrator;
    }

    public NarrateAction setEntity(String entityName) {
        this.entityName = requireNonNull(entityName, "entityName");
        return this;
    }

    @Override public void setParameter(String param) {
        Matcher matcher = ENTITY_NAME_PATTERN.matcher(param);
        if (matcher.find()) {
            entityName = matcher.group(1);
            param = matcher.replaceAll("");
        }
        super.setParameter(param);
    }

    @Override public CompletionStage<Void> execute(ActionContext context) {
        Locatable locatable = (Locatable) context.getCommandSource();
        World world = locatable.getWorld();

        return narrator.narrate(world, requireNonNull(entityName, "entityName"), getText().toPlain(), context.getReadingSpeed());
    }

    @Override public boolean add(Action<?> action) {
        if (action instanceof TextAction) {
            this.setText(getText().concat(Text.NEW_LINE).concat(((TextAction<?>) action).getText()));
            return true;
        }
        return false;
    }

    public String getEntityName() {
        return entityName;
    }

    @Override public String toString() {
        return "\"" + super.toString() + "\" says: " + entityName;
    }
}
