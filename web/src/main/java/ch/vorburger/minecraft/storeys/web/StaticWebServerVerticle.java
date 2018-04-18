/**
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

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vert.x Verticle serving static content.
 *
 * @author Michael Vorburger.ch
 */
public class StaticWebServerVerticle extends AbstractHttpServerVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(StaticWebServerVerticle.class);

    private final String webRoot;

    public StaticWebServerVerticle(int httpPort) {
        super(httpPort);
        this.webRoot = "static"; // ~= ../scratch/dist/*.js
    }

    public StaticWebServerVerticle(int httpPort, File webRoot) {
        super(httpPort);
        this.webRoot = webRoot.getPath();
    }

    @Override
    protected void addRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create().setDirectoryListing(true).setWebRoot(webRoot));
        LOG.info("Going to serve static web content from {} on port {}", webRoot, httpPort);
    }

}
