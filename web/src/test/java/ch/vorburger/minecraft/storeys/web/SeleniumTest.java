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

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;

import ch.vorburger.minecraft.osgi.api.PluginInstance;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.events.EventService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Integration test, based on WebDriver.
 * This does not test Minecraft itself nor the ScratchX UI but the code in this project which sits between the two.
 *
 * @author Michael Vorburger.ch
 */
public class SeleniumTest {

    // TODO start Vert.x-based web server serving static content from ../scratch/
    // but for now just use e.g. "python -m SimpleHTTPServer 9090"

    // TODO use https://www.testcontainers.org

    // TODO @Test public void testLoadInScratchX() {
    //    find some trick to pipe from http://scratchx.org to be able to load local file...
    //    then open http://scratchx.org/?url=... ? (Or does that only work for SBX, not .js ?)

    @Test
    public void testFunctionality() throws Exception {
        VertxStarter vertxStarter = new VertxStarter();
        // TODO use another (random) port and pass URL to minecraft.js via argument
        MockHandler handler = new MockHandler(null, null, mock(EventService.class), null, vertxStarter);
        vertxStarter.start(8080, handler).get();

        // System.setProperty("webdriver.gecko.driver", "/home/vorburger/bin/geckodriver");
        System.setProperty("webdriver.chrome.driver", "/home/vorburger/bin/chromedriver");
        DesiredCapabilities caps = DesiredCapabilities.chrome();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        // TODO how to resolve this deprecated correctly?
        WebDriver webDriver = new ChromeDriver(caps);
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        FluentWait<WebDriver> await = new WebDriverWait(webDriver, 3).pollingEvery(Duration.ofMillis(100));
        try {
            webDriver.get("http://localhost:9090/test/test.html");
            assertThat(webDriver.getTitle()).isEqualTo("Test");
            assertNoBrowserConsoleLogErrors(webDriver);

            // Let's just make sure that WD executeScript() works fine:
            long number = (Long) js.executeScript("return 1 + 2;");
            assertEquals(3, number);

            await.withMessage("scratchMinecraftExtension not ready")
                    .until(ExpectedConditions.jsReturnsValue("return scratchMinecraftExtension !== undefined"));
            // TODO why does await above not work and we need to sleep() anyway?!
            // Without this the next executeScript (sometimes, timing..) fails with "WebDriverException: unknown error: INVALID_STATE_ERR"
            Thread.sleep(500);
            //INVALID_STATE_ERR is a websocket problem

            js.executeScript("return scratchMinecraftExtension.sendTitle('hello, world', function(){ window.sendTitle = true });");
            await.until(ExpectedConditions.jsReturnsValue("return window.sendTitle === true"));

            // TODO assert we really ran a showTitle on the server side...
            // now it fails cause we are not running a server and Test.toPlain is using a DummyClass that throws an exception
            // this is hard to mock because it's a static class

        } finally {
            webDriver.close();
            vertxStarter.stop();
        }
    }

    private void assertNoBrowserConsoleLogErrors(WebDriver webDriver) {
        String firstMessage = null;
        LogEntries logEntries = webDriver.manage().logs().get(LogType.BROWSER);
        for (LogEntry entry : logEntries) {
            System.out.println("BROWSER: " + new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
            if (entry.getLevel().equals(Level.SEVERE)) {
                if (firstMessage == null) {
                    firstMessage = entry.getMessage();
                }
            }
        }
        if (firstMessage != null) {
            throw new AssertionError(firstMessage);
        }
    }

    class MockHandler extends ActionsConsumer {
        private Player player = mock(Player.class);

        MockHandler(PluginInstance plugin, Game game, EventService eventService, ConditionService conditionService, EventBusSender eventBusSender) {
            super(plugin, game, eventService, conditionService, eventBusSender);
        }

        @Override
        Optional<Player> getOptPlayer(String secureCode) {
            return Optional.of(player);
        }
    }
}
