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

import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.util.Command;
import ch.vorburger.minecraft.utils.CommandExceptions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

/**
 * Minecraft console command to login to ScratchX.
 */
public class TokenCommand implements Command {

    private final TokenProvider tokenProvider;

    public TokenCommand(TokenProvider newTokenProvider) {
        this.tokenProvider = newTokenProvider;
    }

    @Override
    public CommandCallable callable() {
        return CommandSpec.builder()
                .permission("storeys.token.new")
                .description(Text.of("Obtain API token for player"))
                .executor(this).build();
    }

    @Override
    public List<String> aliases() {
        return ImmutableList.of("token");
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            CommandExceptions.doOrThrow("loginURL", () -> {
                Player player = (Player)src;

                String token = tokenProvider.getCode(player);

                src.sendMessage(Text.builder("Shift click here to insert your API Token to copy clipboard").onShiftClick(
                        TextActions.insertText(token)).color(TextColors.GREEN).build());
            });
        }
        return CommandResult.empty();
    }

}
