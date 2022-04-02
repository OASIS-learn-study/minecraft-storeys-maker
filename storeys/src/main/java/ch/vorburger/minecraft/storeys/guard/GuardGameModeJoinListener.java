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
package ch.vorburger.minecraft.storeys.guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.network.ClientConnectionEvent.Join;

/**
 * Listener which sets player game mode based on permission (if any) on join.
 *
 * @author Michael Vorburger.ch
 */
public class GuardGameModeJoinListener implements EventListener<Join> {

    private static final Logger LOG = LoggerFactory.getLogger(GuardGameModeJoinListener.class);

    @Override public void handle(Join joinEvent) throws Exception {
        GameMode newGameMode = null;
        Player player = joinEvent.getTargetEntity();
        // NB: Order and use of if and not else if - because higher permission overrides lower...
        // But beware that OPS on servers without permissions plugin (such as LuckPerms) have all permissions;
        // therefore the last permission must be the one which we wants an OPS to have!
        if (player.hasPermission("storeys.guard.adventure")) {
            newGameMode = GameModes.ADVENTURE;
        }
        if (player.hasPermission("storeys.guard.creative")) {
            newGameMode = GameModes.CREATIVE;
        }
        if (newGameMode != null) {
            player.offer(Keys.GAME_MODE, newGameMode);
            LOG.info("Setting Player {} game mode to {} due to their storeys.guard.* permissions", player.getName(), newGameMode);
        }
    }

}
