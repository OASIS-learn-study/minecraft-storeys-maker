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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vert.x Verticle serving static content.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class StaticWebServerVerticle extends AbstractHttpServerVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(StaticWebServerVerticle.class);

    private final String webRoot;
    private final Path configDir;

    @Inject public StaticWebServerVerticle(Path configDir, @Named("web-http-port") int httpPort) {
        this.configDir = configDir;
        super(httpPort);
        webRoot = "static"; // = ../scratch3/node_modules/minecraft-storeys-scratch-gui/build/
        this.configDir = configDir;
    }

    @Override protected void addRoutes(Router router) {
        // see https://github.com/vorburger/minecraft-storeys-maker/issues/97 re. setFilesReadOnly(false) &
        String projectPath = "/project/:userId";
        router.get("/project/:userId/end").handler(context -> {
            final String userId = context.pathParam("userId");
            new ProjectEditor(userId, configDir).moveProjectToBackend();
            context.response().send();
        });
        router.get("/project/init").handler(context -> {
            final InputStream stream = getClass().getResourceAsStream("/project.json");
            try {
                context.response().send(IOUtils.toString(stream));
            } catch (IOException e) {
                throw new RuntimeException("Couldn't read initial project.json from jar file", e);
            }
            context.response().end();
        });
        router.put(projectPath).handler(BodyHandler.create().setMergeFormAttributes(true));
        router.put(projectPath).handler(context -> {
            String userId = context.pathParam("userId");
            final String projectJson = context.getBodyAsString();

            final Path workingProject = new ProjectEditor(userId, configDir).getWorkingLocation();
            try {
                if (!Files.exists(workingProject)) {
                    workingProject.toFile().createNewFile();
                }
                new ZipUtility(workingProject.toFile()).addOrReplaceEntry("project.json", new ByteArrayInputStream(projectJson.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new RuntimeException("Couldn't create / update scratch project", e);
            }
            context.response().send(userId);
        });

        router.get(projectPath).handler(context -> {
            String userId = context.pathParam("userId");
            ProjectEditor editor = new ProjectEditor(userId, configDir);

            if (editor.hasBackendFile()) {
                final HttpServerResponse response = context.response();
                Buffer buffer = Buffer.buffer();
                try {
                    editor.moveProjectToWorking();

                    readProjectJson(editor.getWorkingLocation().toFile(), buffer);
                    response.setChunked(true);
                    response.write(buffer);
                    response.end();
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't move / read file in working directory", e);
                }
            } else {
                context.reroute("/project/init");
            }
        });

        LOG.info("Going to serve static web content from {} on port {}", webRoot, httpPort);
    }


    private void readProjectJson(File zipFile, Buffer buf) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(zipFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ZipInputStream zin = new ZipInputStream(bufferedInputStream);
        ZipEntry ze;
        while ((ze = zin.getNextEntry()) != null) {
            if (ze.getName().equals("project.json")) {
                byte[] buffer = new byte[9000];
                int len;
                while ((len = zin.read(buffer)) != -1) {
                    buf.appendBytes(buffer, 0, len);
                }
                break;
            }
        }
        zin.close();
    }

}
