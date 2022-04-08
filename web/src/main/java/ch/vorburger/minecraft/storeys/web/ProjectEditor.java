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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectEditor {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectEditor.class);
    private static final String SCRATCH_PROJECT_EXTENSION = ".sb3";

    private final String playerId;
    private final Path configDir;

    ProjectEditor(String playerId, Path configDir) {
        this.playerId = playerId;
        this.configDir = configDir;
    }

    public void moveProjectToWorking() {
        moveProject(getBackendLocation(), getWorkingLocation());
    }

    public void moveProjectToBackend() {
        moveProject(getWorkingLocation(), getBackendLocation());
    }

    public boolean hasBackendFile() {
        return Files.exists(getBackendLocation());
    }

    public boolean hasWorkingFile() { return Files.exists(getWorkingLocation()); }

    private void moveProject(Path source, Path destination) {
        try {
            Files.move(source, destination);
        } catch (IOException e) {
            LOG.error("Could not move project to folder", e);
        }
    }

    public Path getWorkingLocation() {
        final Path scratchProjects = configDir.resolve("scratch");
        final Path working = scratchProjects.resolve("..").resolve("working");

        if (!Files.exists(working)) {
            try {
                Files.createDirectory(working);
            } catch (IOException e) {
                LOG.error("Could not create working directory", e);
            }
        }
        return working.resolve(playerId + SCRATCH_PROJECT_EXTENSION);
    }

    private Path getBackendLocation() {
        Path scratchProjects = configDir.resolve("scratch");
        return scratchProjects.resolve(playerId + SCRATCH_PROJECT_EXTENSION);
    }
}
