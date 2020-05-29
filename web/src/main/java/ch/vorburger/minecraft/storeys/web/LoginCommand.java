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

import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.util.Command;
import ch.vorburger.minecraft.utils.CommandExceptions;
import com.google.common.collect.ImmutableList;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

/**
 * Minecraft console command to login to ScratchX.
 */
public class LoginCommand implements Command {

    private String scratchGui = "http://localhost:7070/index.html";
    private String eventBusURL = "http://localhost:8080";
    private String encodedEventBusURL;

    private final TokenProvider tokenProvider;

    public LoginCommand(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        scratchGui = getSystemPropertyEnvVarOrDefault("storeys_gui", scratchGui);
        eventBusURL = getSystemPropertyEnvVarOrDefault("storeys_eventBusURL", eventBusURL);
        try {
            encodedEventBusURL = URLEncoder.encode(eventBusURL, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("WTF; no UTF-8?!", e);
        }
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

    @Override
    public CommandCallable callable() {
        return CommandSpec.builder()
                .description(Text.of("Login into ScratchX web interface"))
                .permission("storeys.command.make")
                .arguments(GenericArguments.flags().permissionFlag("storeys.command.make.beta", "b").buildWith(GenericArguments.none()))
                .executor(this).build();
    }

    @Override
    public List<String> aliases() {
        return ImmutableList.of("make", "scratch");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            CommandExceptions.doOrThrow("loginURL", () -> {
                Player player = (Player)src;

                String code = tokenProvider.getCode(player);
                String url = String.format("%s?code=%s&eventBusURL=%s", scratchGui, code, encodedEventBusURL);

                src.sendMessage(Text.builder("Click here to open Scratch and MAKE actions").onClick(
                        TextActions.openUrl(new URL(url))).color(TextColors.GOLD).build());
            });
        } else {
            src.sendMessage(Text.builder("Command source must be Player").color(TextColors.RED).build());
        }
        return CommandResult.empty();
    }

}
