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

import static com.google.common.base.Charsets.UTF_8;

import ch.vorburger.minecraft.storeys.model.LocationToolAction;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.simple.impl.NotLoggedInException;
import com.google.common.io.Files;
import io.vertx.core.Context;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
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
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.plugin.PluginContainer;

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
    private final PluginContainer pluginContainer;

    @Inject public StaticWebServerVerticle(Path configDir, @Named("web-http-port") int httpPort, TokenProvider tokenProvider,
            PluginContainer pluginContainer) {
        super(httpPort);
        webRoot = "static"; // = ../blockly/dist/
        this.tokenProvider = tokenProvider;
        this.pluginContainer = pluginContainer;
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

        router.route().failureHandler(ctx -> {
            final Throwable failure = ctx.failure();
            if (failure != null) {
                LOG.error("HTTP {} {} failed with status {}", ctx.request().method(), ctx.request().path(),
                        ctx.statusCode(), failure);
            }
        });

        router.route("/code/when_inside/:name").handler(ctx -> {
            final String name = ctx.request().getParam("name");
            final Context vertxContext = ctx.vertx().getOrCreateContext();
            final String playerUuid = playerUuidFrom(ctx.user());
            if (playerUuid == null || playerUuid.isEmpty()) {
                LOG.warn("when_inside '{}': JWT has no playerUUID (re-login with /make in-game)", name);
                ctx.fail(401);
                return;
            }
            final UUID uuid;
            try {
                uuid = UUID.fromString(playerUuid);
            } catch (IllegalArgumentException e) {
                LOG.warn("when_inside '{}': invalid playerUUID '{}'", name, playerUuid, e);
                ctx.fail(400);
                return;
            }
            Sponge.server().scheduler().executor(pluginContainer).execute(() -> {
                try {
                    final ServerPlayer player = Sponge.server().player(uuid).orElse(null);
                    if (player == null) {
                        LOG.warn("when_inside '{}': player {} is not online", name, uuid);
                        vertxContext.runOnContext(v -> ctx.fail(404));
                        return;
                    }
                    new LocationToolAction(name).createTool(player);
                    vertxContext.runOnContext(v -> ctx.response().end());
                } catch (Exception e) {
                    LOG.error("when_inside '{}': failed to give location tool to {}", name, uuid, e);
                    vertxContext.runOnContext(v -> ctx.fail(e));
                }
            });
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
            ctx.response().putHeader("Content-Type", "application/xml");
            if (java.nio.file.Files.exists(workspaceFile)) {
                try {
                    ctx.response().end(Files.asCharSource(workspaceFile.toFile(), UTF_8).read());
                } catch (IOException e) {
                    ctx.fail(e);
                }
            } else {
                ctx.response().end("<xml xmlns=\"http://www.w3.org/1999/xhtml\"></xml>");
            }
        });

        // see https://github.com/vorburger/minecraft-storeys-maker/issues/97 re. setFilesReadOnly(false) &
        // setCachingEnabled(false)
        router.route("/*").handler(
                StaticHandler.create().setDirectoryListing(true).setWebRoot(webRoot).setCachingEnabled(false).setFilesReadOnly(false));
        LOG.info("Going to serve static web content from {} on port {}", webRoot, httpPort);
    }

    private static String playerUuidFrom(User user) {
        if (user == null) {
            return null;
        }
        final Object claim = user.get("playerUUID");
        if (claim != null) {
            return claim.toString();
        }
        return user.principal().getString("playerUUID");
    }

    private void fileUpload(Path uploadFolder, Set<FileUpload> fileUploads, File dest) throws IOException {
        for (FileUpload f : fileUploads) {
            LOG.info("Uploaded file {} (size {})", f.uploadedFileName(), f.size());
            // NB: Use Guava Files, not JDK NIO Files, until https://github.com/vorburger/ch.vorburger.fswatch/issues/95 is fixed!
            Files.move(uploadFolder.resolve(f.uploadedFileName()).toFile(), dest);
        }
    }
}
