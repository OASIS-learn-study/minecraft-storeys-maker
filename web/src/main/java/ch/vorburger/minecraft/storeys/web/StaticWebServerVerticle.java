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

import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.simple.impl.NotLoggedInException;
import com.google.common.io.Files;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.StaticHandler;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
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

        JWTAuthOptions authConfig = new JWTAuthOptions().setKeyStore(
                new KeyStoreOptions().setType("jceks").setPath("keystore.jceks").setPassword("_2y47[-53YLf}/frv.Q\""));

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

        router.post("/code/upload").handler(ctx -> {
            final String playerUUID = ctx.user().get("playerUUID");
            ctx.response().putHeader("Content-Type", "text/plain");
            ctx.response().setChunked(true);

            for (FileUpload f : ctx.fileUploads()) {
                LOG.info("Uploaded file {} (size {})", f.uploadedFileName(), f.size());
                try {
                    // NB: Use Guava Files, not JDK NIO Files, until https://github.com/vorburger/ch.vorburger.fswatch/issues/95 is fixed!
                    Files.move(uploadFolder.resolve(f.uploadedFileName()).toFile(),
                            configDir.resolve("new-scripts").resolve(playerUUID).toFile());
                } catch (IOException e) {
                    ctx.fail(e);
                }
            }

            ctx.response().end();
        });

        // see https://github.com/vorburger/minecraft-storeys-maker/issues/97 re. setFilesReadOnly(false) &
        // setCachingEnabled(false)
        router.route("/*").handler(
                StaticHandler.create().setDirectoryListing(true).setWebRoot(webRoot).setCachingEnabled(false).setFilesReadOnly(false));
        LOG.info("Going to serve static web content from {} on port {}", webRoot, httpPort);
    }

}
