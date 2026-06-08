const { test, expect } = require('@playwright/test');

test("test blockly by creating command", async ({ page }) => {
  const { URL } = process.env;

  await page.goto(URL, { waitUntil: "load" });
  await page.getByText("Loading...").waitFor({ state: "hidden", timeout: 60000 });
  await page.getByText("Events").waitFor({ timeout: 60000 });

  await page.getByText("Events").click({ force: true });
  const whenEvent = page.getByText("When /").first();
  await whenEvent.click({ force: true });

  await page.getByText("Actions").click({ force: true });
  const title = page.getByText("title").first();
  await title.click({ force: true });

  await page.locator("text:has-text('abc') >> nth=0").click({ force: true });
  await page.keyboard.type("automated test!");

  const workspaceWhen = page.locator(".blocklyWorkspace").getByText("When /").first();
  const uploadPromise = page.waitForResponse(
    (resp) => resp.url().includes("/code/upload") && resp.ok(),
    { timeout: 10000 }
  );

  await title.dragTo(workspaceWhen, {
    force: true,
    targetPosition: {
      x: 20,
      y: 20,
    },
  });

  const code = page.locator("textarea");
  await expect(code).toContainText('e.whenCommand("demo", function(m) {');
  await expect(code).toContainText("m.title('automated test!');");

  await uploadPromise;
});
