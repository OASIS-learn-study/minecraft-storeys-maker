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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import ch.vorburger.minecraft.storeys.api.HandType;
import ch.vorburger.minecraft.storeys.api.ItemType;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.web.test.TestMinecraft;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Integration test, based on WebDriver.
 * This does not test Minecraft itself nor much of the Blockly UI but the code in this project which sits between.
 *
 * @author Michael Vorburger.ch
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // required because tests are not yet completely independent, see sleep()
public class SeleniumTest {

    // TODO use https://www.testcontainers.org ?

    private static VertxStarter vertxStarter;
    private static TestMinecraft testMinecraft;

    private static WebDriver webDriver;
    private static JavascriptExecutor js;
    private static FluentWait<WebDriver> awaitWD;

    @BeforeClass public static void setupClass() throws Exception {
        startVertx();
        startWebDriver();
    }

    private static void startVertx() throws Exception {
        testMinecraft = new TestMinecraft();
        vertxStarter = new VertxStarter();
        vertxStarter.deployVerticle(new StaticWebServerVerticle(Paths.get("/tmp/storeys/SeleniumTest/config"), 9090, new TokenProvider() {

            @Override public String getCode(Player player) {
                return UUID.randomUUID().toString();
            }

            @Override public String login(String code) {
                return UUID.randomUUID().toString();
            }
        })).toCompletableFuture().get();
    }

    private static void startWebDriver() {
        // see https://github.com/bonigarcia/webdrivermanager
        WebDriverManager.chromedriver().setup();

        // set up WebDriver
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);

        ChromeOptions options = new ChromeOptions();
        options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");

        webDriver = new ChromeDriver(options);
        js = (JavascriptExecutor) webDriver;
        awaitWD = new WebDriverWait(webDriver, 3).pollingEvery(Duration.ofMillis(100));

        webDriver.get("http://localhost:9090/index.html?eventBusURL=http%3A%2F%2Flocalhost%3A6060");
    }

    @AfterClass public static void tearDownClass() throws Exception {
        if (webDriver != null) {
            webDriver.close();
        }
        if (vertxStarter != null) {
            vertxStarter.stop();
        }
    }

    @Test public void a_testPageTitle() {
        assertThat(webDriver.getTitle(), is("/make Storeys!"));

        // Let's just make sure that WD executeScript() works fine:
        long number = (Long) js.executeScript("return 1 + 2;");
        assertEquals(3, number);
    }

    @Ignore // TODO replace something Blockly equivalent, to test it is correctly initialized and ready?
    @Test public void a_testBasicSetUp() {
        assertNoBrowserConsoleLogErrors();

        Object value = js.executeScript("return ext !== undefined");
        assertThat(value, is(instanceOf(Boolean.class)));
        assertThat(value, notNullValue());
        awaitUntilJSReturnsValue("scratchMinecraftExtension not ready", "return ext.scratchMinecraftExtension !== undefined");
    }

    // TODO testWhenCommand

    // TODO testAllOtherBlocks...

    private void awaitUntilJSReturnsValue(String message, String javaScript) {
        if (!javaScript.startsWith("return ")) {
            throw new IllegalArgumentException("JS should start with with return : " + javaScript);
        }
        try {
            awaitWD.withTimeout(Duration.ofSeconds(7)).withMessage(message).until(ExpectedConditions.jsReturnsValue(javaScript));
        } catch (TimeoutException e) {
            // If we timed out, it's useful to print the Browser log, and check it for errors
            assertNoBrowserConsoleLogErrors();
            // This re-throw will not be reached if there was a browser, and that's just fine, because we probably have better
            // details
            throw e;
        }
    }

    private static void assertNoBrowserConsoleLogErrors() {
        String firstMessage = null;
        LogEntries logEntries = webDriver.manage().logs().get(LogType.BROWSER);
        for (LogEntry entry : logEntries) {
            System.out.println("BROWSER: " + new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
            String messageInLowerCase = entry.getMessage().toLowerCase();
            if ((entry.getLevel().equals(Level.SEVERE) || messageInLowerCase.contains("error") || messageInLowerCase.contains(
                    "uncaught ")) && firstMessage == null) {
                firstMessage = entry.getMessage();
            }
        }
        if (firstMessage != null) {
            throw new AssertionError(firstMessage);
        }
    }

    public static void main(String[] args) throws Exception {
        startVertx();
        testMinecraft.itemsHeld.put(HandType.MainHand, ItemType.Apple);
        Mains.waitForEnter();
        vertxStarter.stop();
    }
}
