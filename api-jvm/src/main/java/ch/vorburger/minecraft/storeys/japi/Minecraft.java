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
package ch.vorburger.minecraft.storeys.japi;

import org.spongepowered.api.entity.living.player.Player;

// see also the similar but Vert.x-based interface Minecraft in the api/ module
// as well as https://github.com/OASIS-learn-study/minecraft-storeys-maker/blob/develop/api/src/main/typescript/observable-wrapper.ts
public interface Minecraft {

    // This should only include things which are not already easily possible with standard Minecraft Slash Commands.

    // The CommandSource is an "implicit" not an explicit argument; it's hidden passed through from the Events registration.

    // These methods intentionally do not return e.g. a CompletionStage.

    /**
     * Run Minecraft commands, see https://minecraft.fandom.com/wiki/Commands.
     */
    void cmd(String command);

    void title(String text);

    void narrate(String entity, String text);

    // TODO void wait(int seconds);

    // Following are things that are useful "context" for Scripts

    Player player();
}
