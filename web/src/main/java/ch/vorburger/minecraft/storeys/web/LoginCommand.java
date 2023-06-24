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
package ch.vorburger.minecraft.storeys.web;

import ch.vorburger.minecraft.storeys.japi.util.CommandExceptions;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.util.Command;
import java.net.URL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Minecraft console command to login to Scratch.
 */
public class LoginCommand implements Command {

    private String scratchGui = "http://localhost:7070/index.html";

    private final TokenProvider tokenProvider;

    public LoginCommand(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        scratchGui = getSystemPropertyEnvVarOrDefault("storeys_gui", scratchGui);
    }

    private String getSystemPropertyEnvVarOrDefault(String propertyName, String defaultValue) {
        String property = System.getProperty(propertyName);
        if (property != null) {
            return property;
        }
        property = System.getenv(propertyName);
        if (property != null) {
            return property;
        }
        return defaultValue;
    }

    @Override public org.spongepowered.api.command.Command.Parameterized createCommand() {
        return org.spongepowered.api.command.Command.builder().shortDescription(Component.text("Open the browser and start your story"))
                .executor(this).build();
    }

    @Override public String getName() {
        return "make";
    }

    @Override public String[] aliases() {
        return new String[] { "scratch" };
    }

    @Override public CommandResult execute(CommandContext args) throws CommandException {
        Object src = args.associatedObject().orElseGet(null);
        if (src instanceof Player) {
            CommandExceptions.doOrThrow("loginURL", () -> {
                Player player = (Player) src;

                String code = tokenProvider.getCode(player);
                String url = String.format("%s?code=%s", scratchGui, code);

                player.sendMessage(
                        Component.text("Click here to open a browser and start MAKE actions").clickEvent(ClickEvent.openUrl(new URL(url)))
                                .color(NamedTextColor.GOLD));
            });
            return CommandResult.success();
        }
        return CommandResult.error(Component.text("Command source must be Player").color(NamedTextColor.RED));
    }

}
