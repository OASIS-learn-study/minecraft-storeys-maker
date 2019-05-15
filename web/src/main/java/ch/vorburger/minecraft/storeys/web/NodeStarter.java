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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class NodeStarter {

    private static final Logger LOG = LoggerFactory.getLogger(NodeStarter.class);
    private static final String EVENT_BUS_URL_KEY = "storeys_eventBusURL";

    private final String path;
    private final Path configDir;

    public NodeStarter(Path configDir) {
        this("/tmp/src/scratch/.gradle/nodejs/node-v8.11.1-linux-x64/bin/node", configDir);
    }

    public NodeStarter(String nodePath, Path configDir) {
        this.path = nodePath;
        this.configDir = configDir;
    }

    public void start() {
        try {
            LOG.info("executing command: '{} {}'", path, Paths.get(configDir.toString(), "index.js"));

            ProcessBuilder processBuilder = new ProcessBuilder(path, "index.js");
            processBuilder.directory(configDir.toFile());
            Map<String, String> environment = processBuilder.environment();
            if (System.getenv(EVENT_BUS_URL_KEY) == null) {
                environment.put(EVENT_BUS_URL_KEY, System.getProperty(EVENT_BUS_URL_KEY));
                environment.put("code", "learn.study.m1n3craft");
            }

            Process process = processBuilder.start();
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;

                while ((line = input.readLine()) != null) {
                    LOG.error(line);
                }
            }
        } catch (IOException err) {
            throw new RuntimeException("error while staring process", err);
        }
    }
}
