package ch.vorburger.minecraft.storeys.plugin;

import ch.vorburger.minecraft.osgi.api.AbstractPlugin;
import ch.vorburger.minecraft.storeys.commands.NarrateCommand;
import ch.vorburger.minecraft.storeys.commands.StoryCommand;
import ch.vorburger.minecraft.storeys.util.Commands;
import java.nio.file.Path;
import javax.inject.Inject;
import org.slf4j.Logger;
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
public class StoreysPlugin extends AbstractPlugin {

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private CommandMapping narrateCommandMapping;
    private CommandMapping storyCommandMapping;

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        logger.info("See https://github.com/vorburger/minecraft-storeys-maker for how to use /story and /narrate commands");
        storyCommandMapping = Commands.register(this, new StoryCommand(this, configDir));
        narrateCommandMapping = Commands.register(this, new NarrateCommand(this));
    }

    @Listener
    public void onGameStoppingServer(GameStoppingServerEvent event) {
        if (narrateCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(narrateCommandMapping);
        }
        if (storyCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(storyCommandMapping);
        }
    }

}
