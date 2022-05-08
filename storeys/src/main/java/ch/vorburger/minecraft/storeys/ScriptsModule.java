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
package ch.vorburger.minecraft.storeys;

import static java.nio.file.Files.walk;

import ch.vorburger.fswatch.DirectoryWatcher;
import ch.vorburger.fswatch.DirectoryWatcherBuilder;
import ch.vorburger.minecraft.storeys.example.ExampleScript;
import ch.vorburger.minecraft.storeys.japi.Script;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptsModule extends AbstractModule {

    private final Path scriptsFolder;

    public ScriptsModule(Path configPath) {
        this.scriptsFolder = configPath.resolve("new-scripts");
    }

    @Override protected void configure() {
        final Multibinder<Script> scriptsBinder = Multibinder.newSetBinder(binder(), Script.class);
        scriptsBinder.addBinding().to(ExampleScript.class);
        try (DirectoryWatcher dw = new DirectoryWatcherBuilder().path(scriptsFolder).listener((path, changeKind) -> {
            if (changeKind == DirectoryWatcher.ChangeKind.MODIFIED && !Files.isDirectory(path)) {
                //TODO Unregister
                parse(path);
            }
        }).build()) {
            walk(scriptsFolder).filter(Files::isRegularFile).forEach(file -> {
                try {
                    parse(file);
                } catch (ScriptException | IOException e) {
                    throw new RuntimeException("Could not load file", e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parse(Path file) throws IOException, ScriptException {
        String template = fileToText(getClass().getResourceAsStream("/script-template.js"));
        String scriptFile = fileToText(file);

        final String result = template.replace("//SCRIPT", scriptFile);

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        final Script script = (Script) engine.eval(result);
        Multibinder.newSetBinder(binder(), Script.class).addBinding().toInstance(script);
    }

    private String fileToText(Path file) throws IOException {
        final InputStream inputStream = Files.newInputStream(file);

        return fileToText(inputStream);
    }

    private String fileToText(InputStream inputStream) {
        String text;
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            text = scanner.useDelimiter("\\A").next();
        }
        return text;
    }
}

