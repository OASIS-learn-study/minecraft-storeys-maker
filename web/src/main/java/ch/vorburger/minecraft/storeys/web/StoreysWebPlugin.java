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
package ch.vorburger.minecraft.storeys.web;

import ch.vorburger.minecraft.osgi.api.Listeners;
import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.api.Minecraft;
import ch.vorburger.minecraft.storeys.api.impl.MinecraftImpl;
import ch.vorburger.minecraft.storeys.api.impl.TokenCommand;
import ch.vorburger.minecraft.storeys.plugin.AbstractStoreysPlugin;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.simple.impl.TokenProviderImpl;
import ch.vorburger.minecraft.storeys.util.Commands;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "storeys-web", name = "Vorburger.ch's Storeys with Web API", version = "1.0",
    description = "Makes entities narrate story lines so you can make your own movie in Minecraft",
    url = "https://github.com/vorburger/minecraft-storeys-maker",
    authors = "Michael Vorburger.ch")
public class StoreysWebPlugin extends AbstractStoreysPlugin implements Listeners {
    // do not extend StoreysPlugin, because we exclude that class in shadowJar

    private static final Logger LOG = LoggerFactory.getLogger(StoreysWebPlugin.class);

    private VertxStarter vertxStarter;
    private ActionsConsumer actionsConsumer;

    private CommandMapping loginCommandMapping;
    private CommandMapping tokenCommandMapping;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;

    @Override
    public void start(PluginInstance plugin, Path configDir) throws Exception {
        super.start(plugin, configDir);

        Injector injector = pluginInjector.createChildInjector(binder -> {
            binder.bind(TokenProvider.class).to(TokenProviderImpl.class);
            binder.bind(Minecraft.class).to(MinecraftImpl.class);
            binder.bind(EventBusSender.class).to(MinecraftVerticle.class);
            binder.bind(new TypeLiteral<Handler<Message<JsonObject>>>(){}).to(ActionsConsumer.class);
            // TODO read from some configuration
            binder.bind(Integer.class).annotatedWith(Names.named("http-port")).toInstance(8080);
            binder.bind(Integer.class).annotatedWith(Names.named("web-http-port")).toInstance(7070);
            binder.bind(new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>(){}).toInstance(configurationLoader);
            binder.bind(LocationToolListener.class);
        });
        actionsConsumer = injector.getInstance(ActionsConsumer.class);
        MinecraftVerticle minecraftVerticle = injector.getInstance(MinecraftVerticle.class);
        StaticWebServerVerticle staticWebServerVerticle = injector.getInstance(StaticWebServerVerticle.class);

        TokenProvider tokenProvider = injector.getInstance(TokenProvider.class);
        loginCommandMapping = Commands.register(plugin, new LoginCommand(tokenProvider));
        tokenCommandMapping = Commands.register(plugin, new TokenCommand(tokenProvider));

        try {
            try {
                vertxStarter = new VertxStarter();

                vertxStarter.deployVerticle(minecraftVerticle).toCompletableFuture().get();
                vertxStarter.deployVerticle(staticWebServerVerticle).toCompletableFuture().get();

            } catch (ExecutionException  | InterruptedException e) {
                throw new IllegalStateException("Vert.x start-up failed", e);
            }

            new NodeStarter(configDir, plugin).start();
        } catch (RuntimeException e) {
            // If something went wrong during the Vert.x set up, we must unregister the commands registered in super.start()
            // so that, under OSGi, we'll manage to cleanly restart when whatever problem caused the start up to fail is fixed again.
            super.stop();
            throw e;
        }
    }

    @Override
    public void stop() throws Exception {
        if (loginCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(loginCommandMapping);
        }
        if (tokenCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(tokenCommandMapping);
        }
        if (actionsConsumer != null) {
            actionsConsumer.stop();
        }
        if (vertxStarter != null) {
            vertxStarter.stop();
        }
        super.stop();
    }

}
