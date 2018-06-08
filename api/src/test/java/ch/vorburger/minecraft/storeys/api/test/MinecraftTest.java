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
package ch.vorburger.minecraft.storeys.api.test;

import ch.vorburger.minecraft.storeys.api.Minecraft;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ServiceBinder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration test with Vert.x.
 *
 * @author Michael Vorburger.ch
 */
@Ignore // fix test, see https://stackoverflow.com/questions/50706539/how-to-use-async-await-in-vert-x-junit-without-hitting-vertxexception-thread
@RunWith(VertxUnitRunner.class)
public class MinecraftTest {

    @Rule public RunTestOnContext contextRule = new RunTestOnContext();
    @Rule public Timeout timeoutRule = Timeout.seconds(7);

    @Test
    public void testWhenCommand(TestContext testContext) {
        TestMinecraft testMinecraftServer = new TestMinecraft();
        new ServiceBinder(contextRule.vertx()).setAddress(Minecraft.ADDRESS).register(Minecraft.class, testMinecraftServer);

        Minecraft minecraftAPI = Minecraft.createProxy(contextRule.vertx());

        Async commandRegistrationAsync = testContext.async();
        minecraftAPI.newCommand("", "test", event -> {
            System.out.println("whenCommand callback");
            commandRegistrationAsync.complete();
            System.out.println("whenCommand callback complete");
        });
        commandRegistrationAsync.awaitSuccess();

        testMinecraftServer.invokeCommand("test");
    }

}
