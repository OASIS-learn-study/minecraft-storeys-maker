/**
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Function;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.events.Condition;
import ch.vorburger.minecraft.storeys.events.LocatableInBoxCondition;
import com.google.common.util.concurrent.SettableFuture;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class LocationToolAction implements Action<Void> {

    private Function<LocatableInBoxCondition, LocatableInBoxCondition> function = locatableInBoxCondition -> locatableInBoxCondition;
    private Location<World> boxCorner1;
    private Location<World> boxCorner2;

    public LocationToolAction(PluginInstance plugin) {
        Sponge.getEventManager().registerListener(plugin, InteractBlockEvent.class, event -> {
            final Optional<ItemStackSnapshot> snapshot = event.getCause().getContext().get(EventContextKeys.USED_ITEM);
            snapshot.ifPresent(itemStackSnapshot -> {
                if (itemStackSnapshot.createGameDictionaryEntry().matches(createLocationEventCreateTool())) {
                    Player player = (Player) event.getSource();
                    if (event instanceof InteractBlockEvent.Secondary) {
                        event.getInteractionPoint().ifPresent(vector3d -> boxCorner2 = new Location<>(player.getWorld(), vector3d));
                        player.sendMessage(Text.of("second " + (boxCorner1 != null ? "and first" : "") + " point set"));
                    } else {
                        event.getInteractionPoint().ifPresent(vector3d -> boxCorner1 = new Location<>(player.getWorld(), vector3d));
                        player.sendMessage(Text.of("first " + (boxCorner2 != null ? "and second" : "") + " point set"));
                    }

                    if (boxCorner2 != null && boxCorner1 != null) {
                        function.apply(new LocatableInBoxCondition(player, boxCorner1, boxCorner2));
                    }
                }
            });
        });
    }

    public Function<LocatableInBoxCondition, LocatableInBoxCondition> getLocatableInBoxCondition() {
        return function;
    }

    @Override
    public CompletionStage<Void> execute(ActionContext context) {
        final CommandSource source = context.getCommandSource();
        if (source instanceof Player) {
            Player player = (Player) source;
            ItemStack item = createLocationEventCreateTool();
            player.setItemInHand(HandTypes.MAIN_HAND, item);
            player.sendMessage(Text.of(TextColors.YELLOW, "use this axe to draw the the points where the player should enter"));
        }

        return new CompletableFuture<>();
    }

    private ItemStack createLocationEventCreateTool() {
        final ItemStack item = ItemStack.builder().itemType(ItemTypes.IRON_AXE).build();
        item.offer(Keys.DISPLAY_NAME, Text.of(
                //TODO translation?
                TextColors.BLUE, "Location tool"));
        return item;
    }
}
