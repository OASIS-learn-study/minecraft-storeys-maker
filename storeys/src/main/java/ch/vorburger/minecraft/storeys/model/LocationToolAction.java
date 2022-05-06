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
package ch.vorburger.minecraft.storeys.model;

import static java.util.Collections.singletonList;

import ch.vorburger.minecraft.storeys.japi.impl.actions.Action;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class LocationToolAction implements Action<Void> {

    private final String name;

    public LocationToolAction(String name) {
        this.name = name;
    }

    @Override public CompletionStage<Void> execute(ActionContext context) {
        final CommandSource source = context.getCommandSource();
        if (source instanceof Player) {
            Player player = (Player) source;
            final ItemStack itemInHand = locationEventCreateTool();
            itemInHand.offer(Keys.ITEM_LORE, singletonList(Text.of(name)));
            player.setItemInHand(HandTypes.MAIN_HAND, itemInHand);
            // TODO translation?
            player.sendMessage(Text.of(TextColors.YELLOW, "use this axe to draw the the points where the player should enter"));
        }

        return new CompletableFuture<>();
    }

    @Override public void setParameter(String param) {
    }

    public static ItemStack locationEventCreateTool() {
        final ItemStack item = ItemStack.builder().itemType(ItemTypes.IRON_AXE).build();
        item.offer(Keys.DISPLAY_NAME, Text.of(TextColors.BLUE, "Location tool"));
        return item;
    }
}
