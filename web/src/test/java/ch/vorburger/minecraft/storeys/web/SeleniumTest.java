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
import static org.junit.Assert.assertEquals;

import ch.vorburger.minecraft.storeys.api.Minecraft;
import ch.vorburger.minecraft.storeys.api.impl.MinecraftImpl;
import ch.vorburger.minecraft.storeys.events.ConditionService;
import ch.vorburger.minecraft.storeys.simple.TokenProvider;
import ch.vorburger.minecraft.storeys.web.test.TestMinecraft;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import java.time.Duration;
import java.util.Date;
import java.util.function.Function;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.JavascriptExecutor;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration test, based on WebDriver.
 * This does not test Minecraft itself nor the ScratchX UI but the code in this project which sits between the two.
 *
 * @author Michael Vorburger.ch
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // required because tests are not yet completely independent, see sleep()
public class SeleniumTest {

    private static final Logger LOG = LoggerFactory.getLogger(SeleniumTest.class);

    // TODO use https://www.testcontainers.org

    // TODO @Test public void testLoadInScratchX() {
    //    find some trick to pipe from http://scratchx.org to be able to load local file...
    //    then open http://scratchx.org/?url=... like LoginCommand does.

    private static VertxStarter vertxStarter;
    private static TestMinecraft testMinecraft;

    private static WebDriver webDriver;
    private static JavascriptExecutor js;
    private static FluentWait<WebDriver> awaitWD;

    @BeforeClass
    public static void setupClass() throws Exception {
        // see https://github.com/bonigarcia/webdrivermanager
        WebDriverManager.chromedriver().setup();

        testMinecraft = new TestMinecraft();
        vertxStarter = new VertxStarter();
        // TODO use another (random) port and pass URL to minecraft.js via argument
        MinecraftVerticle minecraftVerticle = new MinecraftVerticle(8080, testMinecraft);
        minecraftVerticle.setActionsConsumer(event -> LOG.warn("Received event, but ignoring/not handling in this test: {}", event));
        vertxStarter.deployVerticle(minecraftVerticle).toCompletableFuture().get();
        vertxStarter.deployVerticle(new StaticWebServerVerticle(9090, new File("../scratch/dist"))).toCompletableFuture().get();

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

        webDriver.get("http://localhost:9090/index.html?evenbusUrl=http%3A%2F%2Flocalhost%3A8080%2Feventbus");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        webDriver.close();
        vertxStarter.stop();
    }

    @Test
    public void a_testBasicSetUp() throws Exception {
        assertThat(webDriver.getTitle()).isEqualTo("Test");
        assertNoBrowserConsoleLogErrors();

        // Let's just make sure that WD executeScript() works fine:
        long number = (Long) js.executeScript("return 1 + 2;");
        assertEquals(3, number);

        Object value = js.executeScript("return ext !== undefined");
        assertThat(value).isInstanceOf(Boolean.class);
        assertThat(value).isNotNull();
        awaitUntilJSReturnsValue("scratchMinecraftExtension not ready", "return ext !== undefined");
        // TODO why does await above not work and we need to sleep() anyway?!
        // Without this the next executeScript (sometimes, timing..) fails with "WebDriverException: unknown error: INVALID_STATE_ERR"
        Thread.sleep(500);
    }

    @Test
    public void b_testSendTitle() throws Exception {
        js.executeScript("ext.scratchMinecraftExtension.sendTitle('hello, world', ext.callback('sendTitle'))");
        awaitUntilJSReturnsValue("callback not yet invoked", "return ext.isCallbackCalled('sendTitle')");
        assertThat(testMinecraft.lastTitle).isEqualTo("hello, world");
    }

    @SuppressWarnings("unchecked")
    private void awaitUntilJSReturnsValue(String message, String javaScript) {
        // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=534966
        awaitWD.withMessage(message).until((Function<? super WebDriver, Object>) ExpectedConditions.jsReturnsValue(javaScript));
    }

    // TODO testWhenCommand

    // TODO testAllOtherBlocks...

    private static void assertNoBrowserConsoleLogErrors() {
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

}
