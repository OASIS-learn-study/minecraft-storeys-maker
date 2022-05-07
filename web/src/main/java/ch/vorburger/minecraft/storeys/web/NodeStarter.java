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

import ch.vorburger.exec.ManagedProcessBuilder;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.minecraft.osgi.api.PluginInstance;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.spongepowered.api.Sponge;

@Singleton public class NodeStarter {

    void npm(Path configDir, String arg) throws ManagedProcessException {
        new ManagedProcessBuilder("npm").addArgument(arg).setWorkingDirectory(configDir.toFile()).build().start().waitForExit();
    }

    @Inject public void start(Path configDir, PluginInstance plugin) {
        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            try {
                Path packageJson = configDir.resolve("package.json");
                if (Files.notExists(packageJson)) {
                    Files.copy(getClass().getResourceAsStream("/package.json"), packageJson);
                }

                npm(configDir, "install");
                npm(configDir, "start");

            } catch (IOException | ManagedProcessException e) {
                throw new RuntimeException("could not install and run node", e);
            }
        });
    }
}
