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

import ch.vorburger.minecraft.storeys.api.HandType;
import ch.vorburger.minecraft.storeys.api.ItemType;
import ch.vorburger.minecraft.storeys.web.test.TestMinecraft;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
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
        startVertx();
        startWebDriver();
    }

    private static void startVertx() throws Exception {
        testMinecraft = new TestMinecraft();
        vertxStarter = new VertxStarter();
        MinecraftVerticle minecraftVerticle = new MinecraftVerticle(6060, testMinecraft);
        minecraftVerticle.setActionsConsumer(event -> LOG.warn("Received event, but ignoring/not handling in this test: {}", event.body()));
        vertxStarter.deployVerticle(minecraftVerticle).toCompletableFuture().get();
        vertxStarter.deployVerticle(new StaticWebServerVerticle(9090, new File("../scratch/dist"))).toCompletableFuture().get();
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

        webDriver.get("http://localhost:9090/index.html?eventBusURL=http%3A%2F%2Flocalhost%3A6060%2Feventbus");
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
    public void b_testSendTitle() {
        final String message = "hello, world";
        testEventBusCall("sendTitle", message);

        assertThat(testMinecraft.results.get("lastTitle")).isEqualTo(message);
    }

    @Test
    public void c_testNarrate() {
        final String text = "Hi and welcome to Minecraft";
        final String entity = "joe";
        testEventBusCall("narrate", entity, text);
        assertThat(testMinecraft.results.get("entity")).isEqualTo(entity);
        assertThat(testMinecraft.results.get("text")).isEqualTo(text);
    }

    @Test
    public void e_testNegativeAPI() {
        assertThat(runTesterJSAndGetFailures()).containsExactly("getItemHeld expected Apple but actually got Nothing");
    }

    @Test
    public void f_testPositiveAPI() {
        testMinecraft.itemsHeld.put(HandType.MainHand, ItemType.Apple);
        assertThat(runTesterJSAndGetFailures().isEmpty());
    }

    @Test
    public void g_testEncoding() {
        // Test encoding problems, see https://github.com/vorburger/minecraft-storeys-maker/issues/92
        String nonTrivialCommand = "give michaelpapa7 written_book 1 0 {pages:[\"{\\\"text\\\":\\\"Hello\\\"}\"],title:\"First Quest\",author:\"https://www.learn.study\",display:{Lore:[\"The first quest is your first test...\"]}}";
        testEventBusCall("minecraftCommand", nonTrivialCommand);
        assertThat(testMinecraft.ranCommands).contains(nonTrivialCommand);
    }

    // TODO testWhenCommand

    // TODO testAllOtherBlocks...

    @SuppressWarnings("unchecked")
    private List<String> runTesterJSAndGetFailures() {
        js.executeScript("tester.test()");
        assertNoBrowserConsoleLogErrors();
        awaitUntilJSReturnsValue("Client side test is not yet done", "return tester.isDone() === true");
        return (List<String>) js.executeScript("return tester.failures");
    }

    private void testEventBusCall(String function, String... params) {
        String script = "ext.scratchMinecraftExtension.%s(''{0}'', ext.callback(''%s''))";
        script = MessageFormat.format(script, String.join("', '", params));
        script = String.format(script, function, function);

        js.executeScript(script);
        assertNoBrowserConsoleLogErrors();
        awaitUntilJSReturnsValue("callback not yet invoked", String.format("return ext.isCallbackCalled('%s')", function));
    }

    private void awaitUntilJSReturnsValue(String message, String javaScript) {
        if (!javaScript.startsWith("return ")) {
            throw new IllegalArgumentException("JS should start with with return : " + javaScript);
        }
        try {
            awaitWD.withTimeout(Duration.ofSeconds(7)).withMessage(message).until(ExpectedConditions.jsReturnsValue(javaScript));
        } catch (TimeoutException e) {
            // If we timed out, it's useful to print the Browser log, and check it for errors
            assertNoBrowserConsoleLogErrors();
            // This re-throw will not be reached if there was a browser, and that's just fine, because we probably have better details
            throw e;
        }
    }

    private static void assertNoBrowserConsoleLogErrors() {
        String firstMessage = null;
        LogEntries logEntries = webDriver.manage().logs().get(LogType.BROWSER);
        for (LogEntry entry : logEntries) {
            System.out.println("BROWSER: " + new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
            if (entry.getLevel().equals(Level.SEVERE) || entry.getMessage().toLowerCase().contains("error")) {
                if (firstMessage == null) {
                    firstMessage = entry.getMessage();
                }
            }
        }
        if (firstMessage != null) {
            throw new AssertionError(firstMessage);
        }
    }

    public static void main(String[] args) throws Exception {
        startVertx();
        Mains.waitForEnter();
        vertxStarter.stop();
    }
}
