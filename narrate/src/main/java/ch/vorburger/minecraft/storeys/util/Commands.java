package ch.vorburger.minecraft.storeys.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;

public final class Commands {

	private Commands() { }

    public static CommandMapping register(Object plugin, Command command)  throws IllegalStateException {
        return Sponge.getCommandManager().register(plugin, command.callable(), command.aliases())
                .orElseThrow(() -> new IllegalStateException("Failed to register command: " + command.aliases()));
    }

}
