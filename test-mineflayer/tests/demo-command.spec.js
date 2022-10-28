const { test, expect } = require('@playwright/test');

test("test blockly by creating command", async ({ page }) => {
  const { URL } = process.env;

  await page.goto(URL);

  await page.getByText("Events").click({ force: true });
  const whenEvent = page.getByText("When /");
  await whenEvent.click();

  await page.getByText("Actions").click({ force: true });
  const title = page.getByText("title").first();
  await title.click();

  await page.locator("text:has-text('abc') >> nth=0").click({ force: true });
  await page.keyboard.type("automated test!");

  await title.dragTo(whenEvent, {
    force: true,
    targetPosition: {
      x: 20,
      y: 20,
    },
  });

  const code = page.locator("textarea");
  await expect(code).toHaveText(
    `e.whenCommand("demo", function(m) {
    m.title('automated test!');
  
  });`,
    { timeout: 1000 }
  );
});
