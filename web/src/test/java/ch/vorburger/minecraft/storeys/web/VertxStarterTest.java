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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import org.junit.Test;

/**
 * Test the {@link VertxStarter}.
 *
 * @author Michael Vorburger.ch
 */
public class VertxStarterTest {

    @Test public final void testVertxStarterStartStopWithoutVerticle() throws Exception {
        VertxStarter vertxStarter = new VertxStarter();
        vertxStarter.stop();
    }

    @Test
    // NB: This test MAY not pass in-IDE (if the Gradle support doesn't correctly add ../scratch/dist/*.js to the classpath
    // under static/), but it does pass on the build
    public final void testStaticWebContent() throws Exception {
        VertxStarter vertxStarter = new VertxStarter();
        vertxStarter.deployVerticle(new StaticWebServerVerticle(Paths.get("/tmp"), 3030, null)).toCompletableFuture().get();
        assertHTTP("http://localhost:3030/nok", 404);
        assertHTTP("http://localhost:3030/index.html", 200);
        vertxStarter.stop();
    }

    private void assertHTTP(String url, int expectedResponseCode) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(500);
        assertEquals(expectedResponseCode, con.getResponseCode());
    }

    // This main() is only for quick local testing
    public static void main(String[] args) throws Exception {
        VertxStarter vertxStarter = new VertxStarter();

        vertxStarter.deployVerticle(new StaticWebServerVerticle(Paths.get("/tmp/config/storeys-web"), 7070, null)).toCompletableFuture().get();

        Mains.waitForEnter();

        vertxStarter.stop();
    }

}
