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

import ch.vorburger.minecraft.storeys.api.impl.TokenCommand;
import ch.vorburger.minecraft.storeys.plugin.AbstractStoreysPlugin;
import ch.vorburger.minecraft.storeys.plugin.PluginInstance;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.simple.impl.TokenProviderImpl;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("storeys") public class StoreysWebPlugin extends AbstractStoreysPlugin {
    // do not extend StoreysPlugin, because we exclude that class in shadowJar

    private static final Logger LOG = LoggerFactory.getLogger(StoreysWebPlugin.class);

    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    @Inject @DefaultConfig(sharedRoot = true) private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;

    private VertxStarter vertxStarter;
    private Injector webInjector;
    private final TokenProvider tokenProvider = new TokenProviderImpl();

    @Listener public final void onGameStartingServer(StartingEngineEvent<Server> event) throws Exception {
        LOG.info("See https://github.com/OASIS-learn-study/minecraft-storeys-maker for how to use /story and /narrate commands");
        start(this, configDir);
    }

    @Listener public final void onGameStoppingServer(StoppingEngineEvent<Server> event) throws Exception {
        stopWeb();
        stopScripts();
        webInjector = null;
    }

    @Override @Listener public void register(RegisterCommandEvent<Command.Parameterized> event) {
        super.register(event);
        registerWebCommands(event);
    }

    @Override public void start(PluginInstance plugin, Path configDir) {
        super.start(plugin, configDir);

        Injector injector = ensureWebInjector(plugin);
        // Eagerly instantiate listener so it registers block interaction handlers and loads saved locations.
        injector.getInstance(LocationToolListener.class);
        StaticWebServerVerticle staticWebServerVerticle = injector.getInstance(StaticWebServerVerticle.class);

        try {
            vertxStarter = new VertxStarter();
            vertxStarter.deployVerticle(staticWebServerVerticle).toCompletableFuture().get();
        } catch (ExecutionException | InterruptedException e) {
            try {
                stopWeb();
            } catch (Exception stopFailure) {
                e.addSuppressed(stopFailure);
            }
            throw new IllegalStateException("Vert.x start-up failed", e);
        }
    }

    private Injector ensureWebInjector(PluginInstance plugin) {
        if (webInjector == null) {
            webInjector = ensureCommandsInjector(plugin).createChildInjector(binder -> {
                binder.bind(TokenProvider.class).toInstance(tokenProvider);
                // PluginContainer is already bound by Sponge's PluginModule
                // TODO read from some configuration
                binder.bind(Integer.class).annotatedWith(Names.named("http-port")).toInstance(8080);
                binder.bind(Integer.class).annotatedWith(Names.named("web-http-port")).toInstance(7070);
                binder.bind(new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {
                }).toInstance(configurationLoader);
                binder.bind(LocationToolListener.class);
            });
        }
        return webInjector;
    }

    private void registerWebCommands(RegisterCommandEvent<Command.Parameterized> event) {
        LoginCommand loginCommand = new LoginCommand(tokenProvider);
        TokenCommand tokenCommand = new TokenCommand(tokenProvider);
        event.register(getPluginContainer(), loginCommand.createCommand(), loginCommand.getName(), loginCommand.aliases());
        event.register(getPluginContainer(), tokenCommand.createCommand(), tokenCommand.getName(), tokenCommand.aliases());
    }

    private void stopWeb() throws Exception {
        if (vertxStarter != null) {
            vertxStarter.stop();
            vertxStarter = null;
        }
    }
}
