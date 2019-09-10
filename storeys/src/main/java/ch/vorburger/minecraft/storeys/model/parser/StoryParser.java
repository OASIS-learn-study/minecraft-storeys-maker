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

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.AwaitAction;
import ch.vorburger.minecraft.storeys.model.CommandAction;
import ch.vorburger.minecraft.storeys.model.DynamicAction;
import ch.vorburger.minecraft.storeys.model.LocationAction;
import ch.vorburger.minecraft.storeys.model.MessageAction;
import ch.vorburger.minecraft.storeys.model.NarrateAction;
import ch.vorburger.minecraft.storeys.model.Story;
import ch.vorburger.minecraft.storeys.model.TitleAction;
import ch.vorburger.minecraft.storeys.util.MoreStrings;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;

import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.text.Text;

@NotThreadSafe
public class StoryParser {

    private final static Splitter newLineSplitter = Splitter.on('\n');

    private List<Action<?>> actions;
    private NarrateAction narrateActionInConstruction;
    private TitleAction titleActionInConstruction;
    private StringBuilder dynamicActionInConstructionScript;

    private final PluginInstance plugin;
    private final Narrator narrator;

    private final SpongeExecutorService spongeExecutorService;

    public StoryParser(PluginInstance plugin, Narrator narrator, SpongeExecutorService spongeExecutorService) {
        this.plugin = plugin;
        this.narrator = narrator;
        this.spongeExecutorService = spongeExecutorService;
    }

    public StoryParser() {
        this(null, null, null);
    }

    @SuppressWarnings("OrphanedFormatString") // "%await" is a real thing, here
    public Story parse(String storyScript) throws SyntaxErrorException {
        actions = new ArrayList<>();
        narrateActionInConstruction = null;
        titleActionInConstruction = null;
        dynamicActionInConstructionScript = new StringBuilder();

        for (String line : newLineSplitter.split(MoreStrings.normalizeCRLF(storyScript))) {
            if (line.trim().isEmpty()) {
                addActionInConstruction();
            } else if (line.matches("^\\s*//.*")) {
                continue;
            } else if (line.startsWith("==")) {
                // NB: We HAVE to check for "==" before "=" (cauz "==" also startsWith "=")
                String subTitleText = line.substring(2).trim();
                if (titleActionInConstruction == null) {
                    throw new SyntaxErrorException("Subtitle (==) must immediately follow Title (=) : " + subTitleText);
                }
                titleActionInConstruction.setSubtitle(newText(subTitleText));
            } else if (line.startsWith("=")) {
                addActionInConstruction();
                String titleText = line.substring(1).trim();
                titleActionInConstruction = new TitleAction(plugin);
                titleActionInConstruction.setText(newText(titleText));
            } else if (line.startsWith("@")) {
                addActionInConstruction();
                String remainingLine = line.substring(1).trim();
                int firstSpace = remainingLine.indexOf(' ');
                String entityName = remainingLine.substring(0, firstSpace);
                String narrateText = remainingLine.substring(firstSpace);
                narrateActionInConstruction = new NarrateAction(narrator);
                narrateActionInConstruction.setEntity(entityName);
                narrateActionInConstruction.setText(newText(narrateText));
            } else if (line.startsWith("/")) {
                addActionInConstruction();
                String remainingLine = line.substring(1).trim();
                actions.add(new CommandAction(spongeExecutorService).setCommand(remainingLine));
            } else if (line.startsWith("%in")) {
                addActionInConstruction();
                String remainingLine = line.substring("%in".length()).trim();
                String[] coordinates = remainingLine.split("\\s");
                if (coordinates.length != 6) {
                    throw new SyntaxErrorException("region must be 2 coordinates press F3 and write down XYZ for both corners");
                }
                actions.add(new LocationAction(plugin).setBox(remainingLine));
            } else if (line.startsWith("%await")) {
                addActionInConstruction();
                String remainingLine = line.substring("%await".length()).trim();
                if (!remainingLine.endsWith("s")) {
                    throw new SyntaxErrorException("%await currently only supports seconds; example: %await 2s");
                }
                String value = remainingLine.substring(0, remainingLine.length() - 1);
                try {
                    int secsToWait = Integer.decode(value);
                    actions.add(new AwaitAction(plugin).setMsToWait(secsToWait * 1000));
                } catch (NumberFormatException e) {
                    throw new SyntaxErrorException("%await currently only supports numeric value; example: %await 2s; but not: " + value, e);
                }
            } else if (line.matches("^\\s+.*")) {
                dynamicActionInConstructionScript.append(line);
            } else {
                if (narrateActionInConstruction != null) {
                    narrateActionInConstruction.setText(narrateActionInConstruction.getText().concat(Text.NEW_LINE).concat(newText(line)));
                } else {
                    addActionInConstruction();
                    actions.add(new MessageAction(plugin).setText(newText(line)));
                }
            }
        }
        addActionInConstruction();
        return new Story(actions);
    }

    private void addActionInConstruction() {
        if (narrateActionInConstruction != null) {
            actions.add(narrateActionInConstruction);
            narrateActionInConstruction = null;
        } else if (titleActionInConstruction != null) {
            actions.add(titleActionInConstruction);
            titleActionInConstruction = null;
        } else if (dynamicActionInConstructionScript.length() != 0) {
            DynamicAction action = new DynamicAction(plugin);
            action.setScript(dynamicActionInConstructionScript.toString());
            actions.add(action);
            dynamicActionInConstructionScript = new StringBuilder();
        }
    }

    private Text newText(String stringText) {
        return Text.of(stringText);
    }

}
