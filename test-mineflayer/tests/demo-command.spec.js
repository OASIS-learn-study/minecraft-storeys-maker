const { test, expect } = require('@playwright/test');

test("test blockly by creating command", async ({ page }) => {
  const { URL } = process.env;

  await page.goto(URL, { waitUntil: "load" });
  await page.getByText("Loading...").waitFor({ state: "hidden", timeout: 60000 });
  await page.getByText("Events").waitFor({ timeout: 60000 });

  const flyout = page.locator(".blocklyToolboxFlyout");
  const blockCanvas = page.locator(".blocklyBlockCanvas");
  const workspace = page.locator(".injectionDiv > .blocklySvg").first();

  await page.getByText("Events").click({ force: true });
  await page.getByText("When /").first().click({ force: true });

  await page.getByText("Actions").click({ force: true });
  const title = flyout.getByText("title").first();
  const workspaceWhen = blockCanvas.getByText("When /").first();

  // First create the action block from the flyout.
  await title.dragTo(workspaceWhen, {
    force: true,
  });

  // Connect blocks directly via Blockly API to avoid flaky coordinate-based drag behavior.
  await page.evaluate(() => {
    const workspace = window.__storeysBlocklyWorkspace;
    if (!workspace) {
      throw new Error("Blockly workspace is not available");
    }

    const whenCommand = workspace.getTopBlocks(true).find((b) => b.type === "when_command");
    const titleBlock = workspace.getTopBlocks(true).find((b) => b.type === "showTitle");
    if (!whenCommand || !titleBlock) {
      throw new Error("Required Blockly blocks were not created");
    }

    const thenConnection = whenCommand.getInput("THEN")?.connection;
    const titlePreviousConnection = titleBlock.previousConnection;
    if (!thenConnection || !titlePreviousConnection) {
      throw new Error("Missing Blockly block connections");
    }
    thenConnection.connect(titlePreviousConnection);
    workspace.render();
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
  await expect(code).toContainText(
    /e\.whenCommand\("demo", function\(m\) \{\s*m\.title\('automated test!'\);\s*\}\);/,
    { timeout: 15000 }
  );

  await uploadPromise;
});
