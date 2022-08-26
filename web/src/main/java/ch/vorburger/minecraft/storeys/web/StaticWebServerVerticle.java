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

import ch.vorburger.minecraft.storeys.model.LocationToolAction;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.simple.impl.NotLoggedInException;
import com.google.common.io.Files;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.StaticHandler;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Vert.x Verticle serving static content.
 *
 * @author Michael Vorburger.ch
 */
@Singleton public class StaticWebServerVerticle extends AbstractHttpServerVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(StaticWebServerVerticle.class);

    private final String webRoot;
    private final Path configDir;

    private final TokenProvider tokenProvider;

    @Inject public StaticWebServerVerticle(Path configDir, @Named("web-http-port") int httpPort, TokenProvider tokenProvider) {
        super(httpPort);
        webRoot = "static"; // = ../blockly/dist/
        this.tokenProvider = tokenProvider;
        this.configDir = Objects.requireNonNull(configDir, "configDir == null");
    }

    @Override protected void addRoutes(Router router) {
        // see https://github.com/vorburger/minecraft-storeys-maker/issues/97 re. setFilesReadOnly(false) &
        Path uploadFolder;
        try {
            uploadFolder = java.nio.file.Files.createTempDirectory("upload");
            router.route().handler(BodyHandler.create().setUploadsDirectory(uploadFolder.toString()).setMergeFormAttributes(true));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        JWTAuthOptions authConfig = new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions().setAlgorithm("HS256").setBuffer("keyboard cat"));

        JWTAuth authProvider = JWTAuth.create(vertx, authConfig);

        router.route("/login/:code").handler(ctx -> {
            try {
                String playerUUID = tokenProvider.login(ctx.request().getParam("code"));
                ctx.response().end(authProvider.generateToken(new JsonObject().put("playerUUID", playerUUID), new JWTOptions()));
            } catch (NotLoggedInException e) {
                ctx.fail(401);
            }
        });

        router.route("/code/*").handler(JWTAuthHandler.create(authProvider));

        router.route("/code/when_inside/:name").handler(ctx -> {
            final String name = ctx.request().getParam("name");
            final String playerUUID = ctx.user().get("playerUUID");
            final Player player = Sponge.getServer().getPlayer(UUID.fromString(playerUUID))
                    .orElseThrow(() -> new IllegalArgumentException("No player logged in with uuid: " + playerUUID));
            new LocationToolAction(name).createTool(player);
            ctx.response().end();
        });

        router.post("/code/upload").handler(ctx -> {
            final String playerUUID = ctx.user().get("playerUUID");
            ctx.response().putHeader("Content-Type", "text/plain");
            ctx.response().setChunked(true);

            try {
                fileUpload(uploadFolder, ctx.fileUploads(), configDir.resolve("new-scripts").resolve(playerUUID).toFile());
            } catch (IOException e) {
                ctx.fail(e);
            }

            ctx.response().end();
        });

        final Path workspace = configDir.resolve("workspace");
        router.post("/code/workspace/upload").handler(ctx -> {
            final String playerUUID = ctx.user().get("playerUUID");
            ctx.response().putHeader("Content-Type", "text/plain");
            ctx.response().setChunked(true);

            if (!java.nio.file.Files.exists(workspace)) {
                workspace.toFile().mkdirs();
            }

            try {
                fileUpload(uploadFolder, ctx.fileUploads(), workspace.resolve(playerUUID).toFile());
            } catch (IOException e) {
                ctx.fail(e);
            }

            ctx.response().end();
        });

        router.get("/code/workspace").handler(ctx -> {
            final String playerUUID = ctx.user().get("playerUUID");
            final Path workspaceFile = workspace.resolve(playerUUID);
            if (java.nio.file.Files.exists(workspaceFile)) {
                final String fileName = workspaceFile.toString();
                ctx.response().sendFile(fileName).onFailure(t -> LOG.error("sendFile('{}') failed", fileName, t));
            } else {
                ctx.fail(404);
            }
        });

        // see https://github.com/vorburger/minecraft-storeys-maker/issues/97 re. setFilesReadOnly(false) &
        // setCachingEnabled(false)
        router.route("/*").handler(
                StaticHandler.create().setDirectoryListing(true).setWebRoot(webRoot).setCachingEnabled(false).setFilesReadOnly(false));
        LOG.info("Going to serve static web content from {} on port {}", webRoot, httpPort);
    }

    private void fileUpload(Path uploadFolder, Set<FileUpload> fileUploads, File dest) throws IOException {
        for (FileUpload f : fileUploads) {
            LOG.info("Uploaded file {} (size {})", f.uploadedFileName(), f.size());
            // NB: Use Guava Files, not JDK NIO Files, until https://github.com/vorburger/ch.vorburger.fswatch/issues/95 is fixed!
            Files.move(uploadFolder.resolve(f.uploadedFileName()).toFile(), dest);
        }
    }
}
