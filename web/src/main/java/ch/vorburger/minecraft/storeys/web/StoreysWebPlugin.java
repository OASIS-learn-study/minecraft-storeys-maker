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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("storeys") public class StoreysWebPlugin extends AbstractStoreysPlugin {
    // do not extend StoreysPlugin, because we exclude that class in shadowJar

    private static final Logger LOG = LoggerFactory.getLogger(StoreysWebPlugin.class);

    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    @Inject @DefaultConfig(sharedRoot = true) private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;

    @Listener public final void onGameStartingServer(StartingEngineEvent<Server> event) throws Exception {
        LOG.info("See https://github.com/OASIS-learn-study/minecraft-storeys-maker for how to use /story and /narrate commands");
        start(this, configDir);
    }

    @Override public void start(PluginInstance plugin, Path configDir) {
        super.start(plugin, configDir);

        Injector injector = pluginInjector.createChildInjector(binder -> {
            binder.bind(TokenProvider.class).to(TokenProviderImpl.class);
            // TODO read from some configuration
            binder.bind(Integer.class).annotatedWith(Names.named("http-port")).toInstance(8080);
            binder.bind(Integer.class).annotatedWith(Names.named("web-http-port")).toInstance(7070);
            binder.bind(new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {
            }).toInstance(configurationLoader);
            binder.bind(LocationToolListener.class);
            binder.bind(Scheduler.class).toInstance(Sponge.asyncScheduler());
        });
        StaticWebServerVerticle staticWebServerVerticle = injector.getInstance(StaticWebServerVerticle.class);

        TokenProvider tokenProvider = injector.getInstance(TokenProvider.class);
        LoginCommand loginCommand = new LoginCommand(tokenProvider);
        TokenCommand tokenCommand = new TokenCommand(tokenProvider);

        final Optional<CommandRegistrar<Command.Parameterized>> registrar = Sponge.server().commandManager().registrar(Command.Parameterized.class);
        final CommandRegistrar<Command.Parameterized> commandRegistrar = registrar.get();
        commandRegistrar.register(plugin.getPluginContainer(), loginCommand.createCommand(), loginCommand.getName(), loginCommand.aliases());
        commandRegistrar.register(plugin.getPluginContainer(), tokenCommand.createCommand(), tokenCommand.getName(), tokenCommand.aliases());
        try {
            try {
                VertxStarter vertxStarter = new VertxStarter();
                vertxStarter.deployVerticle(staticWebServerVerticle).toCompletableFuture().get();

            } catch (ExecutionException | InterruptedException e) {
                throw new IllegalStateException("Vert.x start-up failed", e);
            }
        } catch (RuntimeException e) {
            // If something went wrong during the Vert.x set up, we must unregister the commands registered in super.start()
            // so that, under OSGi, we'll manage to cleanly restart when whatever problem caused the start up to fail is fixed
            // again.
            throw e;
        }
    }
}
