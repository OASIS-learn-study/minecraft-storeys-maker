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
package ch.vorburger.minecraft.storeys.model.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.inject.Inject;

import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.LocationAction;
import ch.vorburger.minecraft.storeys.model.NarrateAction;
import ch.vorburger.minecraft.storeys.model.Story;
import ch.vorburger.minecraft.storeys.model.TitleAction;
import ch.vorburger.minecraft.storeys.util.MoreStrings;
import com.google.common.base.Splitter;

public class StoryParser {
    private final static Splitter newLineSplitter = Splitter.on('\n').trimResults();

    private final CommandMapping mapping;

    @Inject
    public StoryParser(CommandMapping mapping) {
        this.mapping = mapping;
    }
    
    public Story parse(String story) {
        List<Action<?>> actions = new ArrayList<>();
        for (String line : newLineSplitter.split(MoreStrings.normalizeCRLF(story))) {
            boolean match = false;
            for (CommandMapping.Mapping mapping : mapping.getMappings()) {
                Matcher matcher = mapping.getRegex().matcher(line);
                if (matcher.find()) {
                    Action<?> action = mapping.getActionProvider().get();
                    actions.add(action);
                    action.setParameter(matcher.group(1));
                    match = true;
                    break;
            } else if (line.startsWith("%in")) {
                addActionInConstruction();
                String remainingLine = line.substring("%in".length()).trim();
                String[] coordinates = remainingLine.split("\\s");
                if (coordinates.length != 6) {
                    throw new SyntaxErrorException("region must be 2 coordinates press F3 and write down XYZ for both corners");
                }
                actions.add(new LocationAction(plugin).setBox(remainingLine));
                }
            }
            if (!match) {
                Action<?> lastAction = actions.isEmpty() ? null : actions.get(actions.size() - 1);
                if (lastAction instanceof NarrateAction || lastAction instanceof TitleAction) {
                    lastAction.setParameter(line);
                } else {
                    Action<?> action = mapping.getDefaultAction().get();
                    action.setParameter(line);
                    actions.add(action);
                }
            }
        }
        return new Story(actions);
    }
        } else if (dynamicActionInConstructionScript.length() != 0) {
            DynamicAction action = new DynamicAction(plugin);
            action.setScript(dynamicActionInConstructionScript.toString());
            actions.add(action);
            dynamicActionInConstructionScript = new StringBuilder();
}
