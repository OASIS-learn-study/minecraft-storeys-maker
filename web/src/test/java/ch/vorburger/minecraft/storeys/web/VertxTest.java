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

import ch.vorburger.minecraft.storeys.api.Minecraft;
import ch.vorburger.minecraft.storeys.api.test.TestMinecraft;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ServiceBinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration test with Vert.x (no WebDriver).
 *
 * @author Michael Vorburger.ch
 */
@RunWith(VertxUnitRunner.class)
public class VertxTest {

    // See https://vertx.io/docs/vertx-unit/java/#_junit_integration for the Vert.x magic in this test

    private static final Logger LOG = LoggerFactory.getLogger(VertxTest.class);

    @Rule public RunTestOnContext contextRule = new RunTestOnContext();
    @Rule public Timeout timeoutRule = Timeout.seconds(7);

    private static VertxStarter vertxStarter;
    private static TestMinecraft testMinecraftServer;
    private static Minecraft minecraftAPI;

    @Before
    public void setup(TestContext testContext) throws Exception {
        // Set up logging
        vertxStarter = new VertxStarter(contextRule.vertx());

        testMinecraftServer = new TestMinecraft();

        new ServiceBinder(contextRule.vertx()).setAddress(Minecraft.ADDRESS).register(Minecraft.class, testMinecraftServer);
/*
        // TODO use another (random) port and pass URL to minecraft.js via argument
        MinecraftVerticle minecraftVerticle = new MinecraftVerticle(8080, testMinecraftServer);
        minecraftVerticle.setActionsConsumer(event -> LOG.warn("Received event, but ignoring/not handling in this test: {}", event.body()));
        contextRule.vertx().deployVerticle(minecraftVerticle, testContext.asyncAssertSuccess());
        // no StaticWebServerVerticle in this test
*/
        minecraftAPI = Minecraft.createProxy(vertxStarter.vertx());
    }

    @After
    public void tearDown(TestContext testContext) throws Exception {
        vertxStarter.stop();
        // TODO vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testWhenCommand(TestContext testContext) {
        minecraftAPI.newCommand("", "test", testContext.asyncAssertSuccess());
/* TODO why does this not do the same as above??
        Async whenCommandRegistrationAsync = testContext.async();
        minecraftAPI.whenCommand("", "test", event -> {
            LOG.info("whenCommand callback");
            whenCommandRegistrationAsync.complete();
            LOG.info("whenCommand callback complete");
        });
        whenCommandRegistrationAsync.await(); // TODO .awaitSuccess()
*/
        testMinecraftServer.invokeCommand("test");

/*
        // Test that we can use Handler<AsyncResult<Void>> again
        hit.set(false);
        testMinecraftServer.invokeCommand("test");
        assertThat(hit.get()).isTrue();
*/
    }

}
