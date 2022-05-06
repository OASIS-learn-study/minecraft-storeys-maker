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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.vorburger.minecraft.storeys.StoryPlayer;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ActionContext;
import ch.vorburger.minecraft.storeys.japi.impl.actions.ReadingSpeed;
import ch.vorburger.minecraft.storeys.model.parser.ClassLoaderResourceStoryRepository;
import ch.vorburger.minecraft.storeys.model.parser.StoryParser;
import ch.vorburger.minecraft.storeys.model.parser.StoryParserTest;
import ch.vorburger.minecraft.storeys.model.parser.TestPlainTextSerializer;
import java.io.IOException;
import java.util.concurrent.CompletionStage;
import org.junit.Before;
import org.junit.Test;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.type.CarriedInventory;

public class DynamicActionTest {
    @Before public void initialize() throws Exception {
        TestPlainTextSerializer.inject();
    }

    @Test
    @SuppressWarnings("unchecked") public void execute() throws IOException {
        // given
        StoryParser storyParser = StoryParserTest.getStoryParser();
        String storyText = new ClassLoaderResourceStoryRepository().getStoryScript("dynamic-test");
        DynamicAction dynamicAction = new DynamicAction(storyParser, new StoryPlayer());
        dynamicAction.setParameter(storyText);
        Player commandSource = mock(Player.class);
        CarriedInventory inventory = mock(CarriedInventory.class);
        when(commandSource.getInventory()).thenReturn(inventory);
        when(inventory.contains(ItemTypes.FISHING_ROD)).thenReturn(true);

        // when
        CompletionStage<Void> completionStage = dynamicAction.execute(new ActionContext(commandSource, new ReadingSpeed()));

        // then
        completionStage.thenAccept((aVoid) -> verify(inventory).contains(ItemTypes.FISHING_ROD));
    }
}