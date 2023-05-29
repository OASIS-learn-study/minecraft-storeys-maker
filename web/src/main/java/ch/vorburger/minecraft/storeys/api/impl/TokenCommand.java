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
package ch.vorburger.minecraft.storeys.api.impl;

import ch.vorburger.minecraft.storeys.japi.util.CommandExceptions;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.util.Command;
import com.google.common.collect.ImmutableList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Minecraft console command to login to ScratchX.
 */
public class TokenCommand implements Command {

    private final TokenProvider tokenProvider;

    public TokenCommand(TokenProvider newTokenProvider) {
        this.tokenProvider = newTokenProvider;
    }

    @Override public org.spongepowered.api.command.Command callable() {
        return org.spongepowered.api.command.Command.builder().permission("storeys.token.new")
                .shortDescription(Component.text("Obtain API token for player")).executor(this).build();
    }

    @Override public List<String> aliases() {
        return ImmutableList.of("token");
    }

    @Override public CommandResult execute(CommandContext args) throws CommandException {
        if (args.cause().audience() instanceof Player) {
            CommandExceptions.doOrThrow("loginURL", () -> {
                Player player = (Player) args.cause().audience();

                String token = tokenProvider.getCode(player);

                player.sendMessage(Component.text("Shift click here to insert your API Token to copy clipboard").color(NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, token)));
            });
        } return CommandResult.success();
    }

}
