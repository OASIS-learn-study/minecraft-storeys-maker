package ch.vorburger.minecraft.storeys.model.parser;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.Narrator;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.AwaitAction;
import ch.vorburger.minecraft.storeys.model.CommandAction;
import ch.vorburger.minecraft.storeys.model.MessageAction;
import ch.vorburger.minecraft.storeys.model.NarrateAction;
import ch.vorburger.minecraft.storeys.model.Story;
import ch.vorburger.minecraft.storeys.model.TitleAction;
import ch.vorburger.minecraft.storeys.util.MoreStrings;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;
import org.spongepowered.api.text.Text;

@NotThreadSafe
public class StoryParser {

    private final static Splitter newLineSplitter = Splitter.on('\n').trimResults();

    private List<Action<?>> actions;
    private NarrateAction narrateActionInConstruction;
    private TitleAction titleActionInConstruction;

    private final PluginInstance plugin;
    private final Narrator narrator;

    public StoryParser(PluginInstance plugin, Narrator narrator) {
        super();
        this.plugin = plugin;
        this.narrator = narrator;
    }

    public Story parse(String storyScript) throws SyntaxErrorException {
        actions = new ArrayList<>();
        narrateActionInConstruction = null;
        titleActionInConstruction = null;

        for (String line : newLineSplitter.split(MoreStrings.normalizeCRLF(storyScript))) {
            if (line.isEmpty()) {
                addActionInConstruction();
            } else if (line.startsWith("//")) {
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
                actions.add(new CommandAction().setCommand(remainingLine));
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
        }
    }

    private Text newText(String stringText) {
        return Text.of(stringText);
    }

}
