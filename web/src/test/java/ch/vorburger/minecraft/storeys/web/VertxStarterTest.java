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

import static java.nio.charset.Charset.defaultCharset;
import static org.mockito.Mockito.mock;

import ch.vorburger.minecraft.storeys.events.EventService;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.junit.Test;

/**
 * Test the {@link VertxStarter}.
 *
 * @author Michael Vorburger.ch
 */
public class VertxStarterTest {

    @Test
    public final void testVertxStarter() throws Exception {
        VertxStarter vertxStarter = new VertxStarter();
        vertxStarter.start(6789, new ActionsConsumer(null, mock(EventService.class), null, vertxStarter, null, null)).toCompletableFuture().get();

        vertxStarter.deployVerticle(new StaticWebServerVerticle(2020, new File("../scratch"))).toCompletableFuture().get();
        // new BufferedReader(new InputStreamReader(System.in, defaultCharset())).readLine();
        vertxStarter.stop();
    }

    // This main() is only for quick local testing
    public static void main(String[] args) throws Exception {
        VertxStarter vertxStarter = new VertxStarter();
        vertxStarter.start(8080, new ActionsConsumer(null, mock(EventService.class), null, vertxStarter, null, null)).toCompletableFuture().get();

        vertxStarter.deployVerticle(new StaticWebServerVerticle(9090, new File("../scratch"))).toCompletableFuture().get();

        System.out.println("Running now... press Enter to Stop.");
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in, defaultCharset()));
        buffer.readLine();

        vertxStarter.stop();
    }

}
