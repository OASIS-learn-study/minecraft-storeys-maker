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

import static com.google.common.base.Charsets.UTF_8;

import ch.vorburger.fswatch.DirectoryWatcher;
import ch.vorburger.fswatch.DirectoryWatcherBuilder;
import ch.vorburger.minecraft.storeys.example.ExampleScript;
import ch.vorburger.minecraft.storeys.japi.Script;
import ch.vorburger.minecraft.storeys.japi.impl.Scripts;
import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton public class ScriptsLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ScriptsLoader.class);

    private final DirectoryWatcher dw;

    @Inject public ScriptsLoader(Path configPath, Scripts scripts) throws IOException {
        scripts.register(ExampleScript.class, new ExampleScript());

        Path scriptsFolder = configPath.resolve("new-scripts");
        if (!scriptsFolder.toFile().exists() && !scriptsFolder.toFile().mkdirs()) {
            throw new IOException("Failed to mkdirs: " + scriptsFolder);
        }
        dw = new DirectoryWatcherBuilder().path(scriptsFolder).existingFiles(true).listener((path, changeKind) -> {
            // LOG.info("DirectoryWatcher listener: changeKind={}, path={}", changeKind, path);
            if (!Files.isDirectory(path)) {
                switch (changeKind) {
                    case CREATED:
                    case MODIFIED:
                        scripts.unregister(path);
                        try {
                            scripts.register(path, load(path));
                            LOG.info("(Re-)loaded {}", path);
                        } catch (NoSuchFileException e) {
                            // Ignore (happens frequently for temporary files with Git)
                        } catch (RuntimeException e) {
                            LOG.error("Failed to register due to an unknown cause {}", path, e);
                        }
                        // TODO catch (NashornException e) with getFileName(), getLineNumber(), getColumnNumber()
                        break;

                    case DELETED:
                        if (scripts.unregister(path)) {
                            LOG.info("Unregistered {}", path);
                        }
                        break;

                    default:
                        // Won't happen, just to shut up Checkstyle.
                }
            }
        }).build();
        LOG.info("Watching directory for JS scripts: {}", scriptsFolder);
    }

    private Script load(Path file) throws IOException, ScriptException {
        String template = Resources.toString(Resources.getResource(ScriptsLoader.class, "/script-template.js"), UTF_8);
        String scriptFile = MoreFiles.asCharSource(file, UTF_8).read();

        final String result = template.replace("//SCRIPT", scriptFile);

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("graal.js");

        return (Script) engine.eval(result);
    }

    public void close() {
        dw.close();
    }
}
