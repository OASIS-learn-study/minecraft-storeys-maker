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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.StoryPlayer;
import ch.vorburger.minecraft.storeys.model.parser.StoryParser;
import ch.vorburger.minecraft.storeys.model.parser.SyntaxErrorException;

public class DynamicAction implements Action<Void>  {
    private static final String PREFIX = "var ItemTypes = Java.type('org.spongepowered.api.item.ItemTypes'); (function() {";
    private static final String POSTFIX = "})()";

    private final PluginInstance plugin;
    private String script;

    public DynamicAction(PluginInstance plugin) {
        this.plugin = plugin;
    }

    public DynamicAction setScript(String script) {
        this.script = script;
        return this;
    }

    @Override
    public CompletionStage<Void> execute(ActionContext context) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        engine.put("player", context.getCommandSource());

        try {
            String storyText = (String) engine.eval(PREFIX + script + POSTFIX);
            Story story = new StoryParser().parse(storyText);
            StoryPlayer player = new StoryPlayer(plugin);

            player.play(context, story).thenAccept(o -> future.complete(null));
        } catch (ScriptException | SyntaxErrorException e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + script;
    }
}
