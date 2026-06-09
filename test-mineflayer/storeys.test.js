const mineflayer = require('mineflayer');
const { spawn } = require('node:child_process');

const expect = require('chai').expect

function findClickUrl(json) {
  if (!json) return null;
  if (json.clickEvent?.action === 'open_url') return json.clickEvent.value;
  if (json.json?.clickEvent?.action === 'open_url') return json.json.clickEvent.value;
  for (const part of json.extra || json.json?.extra || []) {
    const url = findClickUrl(part);
    if (url) return url;
  }
  return null;
}

describe("Storeys plugin test", () => {
  let bot;
  let loginURL;

  before((done) => {
    bot = mineflayer.createBot({
      host: "localhost",
      port: 25565
    });
    bot.on("login", done);
  })

  after(() => bot.end());

  it("1 should connect to minecraft server and execute /make", (done) => {
    const onMakeMessage = (msg, _, json) => {
      if (msg !== "Click here to open a browser and start MAKE actions") return;
      bot.removeListener('messagestr', onMakeMessage);
      loginURL = findClickUrl(json);
      expect(loginURL).to.match(/^http:\/\/localhost:7070\/index\.html\?code=/);
      done();
    };
    bot.on('messagestr', onMakeMessage);
    bot.chat("/make");
  });

  it("2 should create new command /demo", (done) => {
    const child = spawn('npm', ['run', 'playwright'], { env: { ...process.env, URL: loginURL } });
    child.stdout.pipe(process.stdout);
    child.stderr.pipe(process.stderr);
    child.on('exit', (code) => {
      setTimeout(() => {
        if (code === 0) done();
        else done(new Error(`playwright exited with code ${code}`));
      }, 5000);
    });
  });

  it("3 should execute /demo", (done) => {
    const onTitle = (msg) => {
      if (!msg.includes("automated test!")) return;
      bot.removeListener('title', onTitle);
      done();
    };
    bot.on('title', onTitle);
    bot.chat("/demo");
  });
});