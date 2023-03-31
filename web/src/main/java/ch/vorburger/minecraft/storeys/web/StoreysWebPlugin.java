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
import ch.vorburger.minecraft.storeys.util.Commands;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

@Plugin("storeys") public class StoreysWebPlugin extends AbstractStoreysPlugin {
    // do not extend StoreysPlugin, because we exclude that class in shadowJar

    private static final Logger LOG = LoggerFactory.getLogger(StoreysWebPlugin.class);
    private VertxStarter vertxStarter;
    private LoginCommand loginCommand;
    private TokenCommand tokenCommand;

    @Inject @DefaultConfig(sharedRoot = true) private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;

    @Override public void start(PluginInstance plugin, Path configDir) throws Exception {
        super.start(plugin, configDir);

        Injector injector = pluginInjector.createChildInjector(binder -> {
            binder.bind(TokenProvider.class).to(TokenProviderImpl.class);
            // TODO read from some configuration
            binder.bind(Integer.class).annotatedWith(Names.named("http-port")).toInstance(8080);
            binder.bind(Integer.class).annotatedWith(Names.named("web-http-port")).toInstance(7070);
            binder.bind(new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {
            }).toInstance(configurationLoader);
            binder.bind(LocationToolListener.class);
        });
        StaticWebServerVerticle staticWebServerVerticle = injector.getInstance(StaticWebServerVerticle.class);

        TokenProvider tokenProvider = injector.getInstance(TokenProvider.class);
        loginCommand = new LoginCommand(tokenProvider);
        tokenCommand = new TokenCommand(tokenProvider);

        try {
            try {
                vertxStarter = new VertxStarter();
                vertxStarter.deployVerticle(staticWebServerVerticle).toCompletableFuture().get();

            } catch (ExecutionException | InterruptedException e) {
                throw new IllegalStateException("Vert.x start-up failed", e);
            }
        } catch (RuntimeException e) {
            // If something went wrong during the Vert.x set up, we must unregister the commands registered in super.start()
            // so that, under OSGi, we'll manage to cleanly restart when whatever problem caused the start up to fail is fixed
            // again.
            super.stop();
            throw e;
        }
    }

    @Listener public void register(RegisterCommandEvent<Command.Raw> event) {
        event.register(this.getPluginContainer(),
                (Command.Raw) loginCommand.callable(), loginCommand.aliases().get(0), loginCommand.aliases().toArray(new String[0]));
    }

    @Override public void stop() throws Exception {
        if (vertxStarter != null) {
            vertxStarter.stop();
        }
        super.stop();
    }

}
