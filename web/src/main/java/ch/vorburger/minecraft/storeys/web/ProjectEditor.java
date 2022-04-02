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

public class ProjectEditor {
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

    private void moveProject(Path scratchProject, Path workingProject) {
        try {
            Files.move(workingProject, scratchProject);
        } catch (IOException e) {
            throw new RuntimeException("Could not move project to working folder", e);
        }
    }

    public Path getWorkingLocation() {
        Path scratchProjects = configDir.resolve("scratch");
        return scratchProjects.resolve("..").resolve("working").resolve(playerId + SCRATCH_PROJECT_EXTENSION);
    }

    private Path getBackendLocation() {
        Path scratchProjects = configDir.resolve("scratch");
        return scratchProjects.resolve(playerId + SCRATCH_PROJECT_EXTENSION);
    }
}
