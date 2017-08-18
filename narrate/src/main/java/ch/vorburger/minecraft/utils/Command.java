package ch.vorburger.minecraft.utils;

import ch.vorburger.minecraft.osgi.api.CommandRegistration;
import org.spongepowered.api.command.spec.CommandExecutor;

public interface Command extends CommandRegistration, CommandExecutor {
}
