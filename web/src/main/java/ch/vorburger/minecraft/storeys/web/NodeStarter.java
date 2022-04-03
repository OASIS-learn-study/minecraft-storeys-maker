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

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import com.github.eirslett.maven.plugins.frontend.lib.FrontendPluginFactory;
import com.github.eirslett.maven.plugins.frontend.lib.InstallationException;
import com.github.eirslett.maven.plugins.frontend.lib.NpmRunner;
import com.github.eirslett.maven.plugins.frontend.lib.ProxyConfig;
import com.github.eirslett.maven.plugins.frontend.lib.TaskRunnerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import org.spongepowered.api.Sponge;

class NodeStarter {
    private static final String NODE_VERSION = "v10.15.3";
    private static final String NPM_VERSION = "6.4.1";
    private final ProxyConfig proxyConfig = new ProxyConfig(new ArrayList<>());

    private final Path configDir;
    private final PluginInstance plugin;

    NodeStarter(Path configDir, PluginInstance plugin) {
        this.configDir = configDir;
        this.plugin = plugin;
    }

    void start() {
        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            try {
                FrontendPluginFactory frontendPluginFactory = new FrontendPluginFactory(configDir.toFile(), new File("/tmp"));
                Path packageJson = configDir.resolve("package.json");
                if (Files.notExists(packageJson)) {
                    Files.copy(getClass().getResourceAsStream("/package.json"), packageJson);
                }

                frontendPluginFactory.getNodeInstaller(proxyConfig).setNodeVersion(NODE_VERSION).install();
                frontendPluginFactory.getNPMInstaller(proxyConfig).setNpmVersion(NPM_VERSION).install();
                NpmRunner npmRunner = frontendPluginFactory.getNpmRunner(proxyConfig, "");

                npmRunner.execute("install", new HashMap<>());
                npmRunner.execute("start", new HashMap<>());
            } catch (TaskRunnerException | InstallationException | IOException e) {
                throw new RuntimeException("could not install and run node", e);
            }
        });
    }
}
