package ch.vorburger.minecraft.storeys.narrate.plugin;

import ch.vorburger.minecraft.osgi.api.AbstractPlugin;
import ch.vorburger.minecraft.storeys.narrate.commands.NarrateCommand;
import ch.vorburger.minecraft.storeys.util.Commands;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "narrate", name = "Vorburger.ch's Storeys Narrator", version = "1.0",
    description = "Makes entities narrate story lines",
    url = "https://github.com/vorburger/minecraft-storeys-maker",
authors = "Michael Vorburger.ch")
public class NarratorPlugin extends AbstractPlugin {

    private CommandMapping narrateCommandMapping;

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        narrateCommandMapping = Commands.register(this, new NarrateCommand(this));
    }

    @Listener
    public void onGameStoppingServer(GameStoppingServerEvent event) {
        Sponge.getCommandManager().removeMapping(narrateCommandMapping);
    }

}
