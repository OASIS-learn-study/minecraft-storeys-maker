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
package ch.vorburger.minecraft.storeys.simple.impl;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.ReadingSpeed;
import ch.vorburger.minecraft.storeys.model.Action;
import ch.vorburger.minecraft.storeys.model.ActionContext;
import ch.vorburger.minecraft.storeys.model.TitleAction;
import ch.vorburger.minecraft.storeys.simple.Minecraft;
import ch.vorburger.minecraft.storeys.simple.Token;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import java.util.concurrent.CompletionStage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

/**
 * Implementation of simple {@link Minecraft} API.
 *
 * @author Michael Vorburger.ch
 */
@Deprecated
public class MinecraftImpl implements Minecraft {

    private final PluginInstance pluginInstance;
    private final TokenProvider tokenProvider;

    public MinecraftImpl(PluginInstance pluginInstance, TokenProvider tokenProvider) {
        this.pluginInstance = pluginInstance;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public CompletionStage<Void> setTitle(Token token, String title) {
        return execute(tokenProvider.getPlayer(token), new TitleAction(pluginInstance).setText(Text.of(title)));
    }

    private <T> CompletionStage<T> execute(CommandSource commandSource, Action<T> action) {
        return action.execute(new ActionContext(commandSource, new ReadingSpeed()));
    }


}
