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
package ch.vorburger.minecraft.storeys.example;

import ch.vorburger.minecraft.storeys.japi.Events;
import ch.vorburger.minecraft.storeys.japi.Script;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * Example Script.
 * Simpler than full-blown Sponge API plugin.
 * While this is written in Java, the idea is that this should look almost identical in TypeScript.
 */
public class ExampleScript implements Script {

    @Override public void init(Events e) {
        e.whenCommand("example", m -> {
            // The following is intentionally on x2 title() - verify that the 2nd awaits the 1st
            m.title("hello,");
            m.title("world");
            m.cmd("/tp 232 63 216 -180 25");
            m.narrate("Piggy", "Hello! I'm Piggy. I have a lot to tell you about... do you want to hear it?");
            m.cmd("/say Message in the Chat");
            m.title("The End");
        });
        e.whenCommand("another", m -> {
            m.title("Namaste. Curry pour tous!");
            if (!m.player().inventory().contains(ItemStack.of(ItemTypes.FISHING_ROD))) {
                m.cmd("/say There may be a fishing rod hidden somewhere… look for it, and then catch a fish!");
            } else {
                m.cmd("/say Go fishing with the rod in your inventory..");
            }
        });

        e.whenEntityRightClicked("Piggy", m -> {
            m.narrate("Piggy", "Oink!");
        });

        // This is intentionally separate and not just an extra line above; it tests that two for the same entity work.
        e.whenEntityRightClicked("Piggy", m -> {
            m.title("Oink!");
        });

        e.whenPlayerJoins(m -> {
            // Do NOT use whenPlayerJoins() with narrate() as that can lead to race conditions in tests; see
            // https://github.com/OASIS-learn-study/minecraft-storeys-maker/issues/401

            // TODO Re-enable this AFTER sorting out /say permission with LuckPerms
            // m.cmd("/say Hej " + m.player().getName());
        });

    }
}
