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

import static com.google.common.truth.Truth.assertThat;

import ch.vorburger.minecraft.storeys.api.Minecraft;
import ch.vorburger.minecraft.storeys.web.test.TestMinecraft;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration test with Vert.x (no WebDriver).
 *
 * @author Michael Vorburger.ch
 */
public class VertxTest {

    // TODO use https://vertx.io/docs/vertx-unit/java/#_junit_integration ?

    private static final Logger LOG = LoggerFactory.getLogger(VertxTest.class);

    private static VertxStarter vertxStarter;
    private static TestMinecraft testMinecraftServer;
    private static Minecraft minecraftAPI;

    @BeforeClass
    public static void setupClass() throws Exception {
        testMinecraftServer = new TestMinecraft();
        vertxStarter = new VertxStarter();
        // TODO use another (random) port and pass URL to minecraft.js via argument
        MinecraftVerticle minecraftVerticle = new MinecraftVerticle(8080, testMinecraftServer);
        minecraftVerticle.setActionsConsumer(event -> LOG.warn("Received event, but ignoring/not handling in this test: {}", event.body()));
        vertxStarter.deployVerticle(minecraftVerticle).toCompletableFuture().get();
        // no StaticWebServerVerticle in this test

        minecraftAPI = Minecraft.createProxy(vertxStarter.vertx());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        vertxStarter.stop();
    }

    @Test
    public void testWhenCommand() {
        AtomicBoolean hit = new AtomicBoolean(false);
        minecraftAPI.whenCommand("", "test", event -> hit.set(true));
        testMinecraftServer.invokeCommand("test");
        assertThat(hit.get()).isTrue();

        // Test that we can use Handler<AsyncResult<Void>> again
        hit.set(false);
        testMinecraftServer.invokeCommand("test");
        assertThat(hit.get()).isTrue();
    }

}
