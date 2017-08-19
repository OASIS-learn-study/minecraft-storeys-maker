package ch.vorburger.minecraft.storeys.narrate.plugin;

import ch.vorburger.minecraft.osgi.api.AbstractPlugin;
import ch.vorburger.minecraft.storeys.narrate.commands.NarrateCommand;
import ch.vorburger.minecraft.storeys.narrate.commands.StoryCommand;
import ch.vorburger.minecraft.storeys.util.Commands;
import java.nio.file.Path;
import javax.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "storeys", name = "Vorburger.ch's Storeys", version = "1.0",
    description = "Makes entities narrate story lines so you can make your own movie in Minecraft",
    url = "https://github.com/vorburger/minecraft-storeys-maker",
authors = "Michael Vorburger.ch")
public class NarratorPlugin extends AbstractPlugin {

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private CommandMapping narrateCommandMapping;
    private CommandMapping storyCommandMapping;

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        storyCommandMapping = Commands.register(this, new StoryCommand(this, configDir));
        narrateCommandMapping = Commands.register(this, new NarrateCommand(this));
    }

    @Listener
    public void onGameStoppingServer(GameStoppingServerEvent event) {
        Sponge.getCommandManager().removeMapping(narrateCommandMapping);
        Sponge.getCommandManager().removeMapping(storyCommandMapping);
    }

}
