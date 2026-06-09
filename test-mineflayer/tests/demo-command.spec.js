const { test, expect } = require('@playwright/test');

test("test blockly by creating command", async ({ page }) => {
  const { URL } = process.env;

  await page.goto(URL, { waitUntil: "load" });
  await page.getByText("Loading...").waitFor({ state: "hidden", timeout: 60000 });
  await page.getByText("Events").waitFor({ timeout: 60000 });

  const flyout = page.locator(".blocklyToolboxFlyout");
  const workspace = page.locator(".injectionDiv > .blocklySvg").first();

  await page.getByText("Events").click({ force: true });
  await page.getByText("When /").first().click({ force: true });

  await page.getByText("Actions").click({ force: true });
  const title = flyout.getByText("title").first();
  const workspaceWhen = workspace.getByText("When /").first();

  await title.dragTo(workspaceWhen, {
    force: true,
    targetPosition: {
      x: 30,
      y: 55,
    },
  });

  // Blockly 12 keeps shadow fields in the flyout hidden; edit after dragging to the workspace.
  await workspace.locator(".blocklyFieldText").filter({ hasText: /^abc$/ }).click({ force: true });
  const textInput = page.locator(".blocklyHtmlInput");
  await textInput.waitFor({ state: "visible", timeout: 5000 });
  const uploadPromise = page.waitForResponse(
    (resp) => resp.url().includes("/code/upload") && resp.ok(),
    { timeout: 15000 }
  );
  await textInput.fill("automated test!");
  await page.keyboard.press("Enter");

  const code = page.locator("textarea");
  await expect(code).toContainText('e.whenCommand("demo", function(m) {', { timeout: 15000 });
  await expect(code).toContainText("m.title('automated test!');");
  await expect(code).not.toContainText("});\nm.title");

  await uploadPromise;
});
