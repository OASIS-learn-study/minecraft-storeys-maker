const mineflayer = require('mineflayer');
const { spawn } = require('node:child_process');

const expect = require('chai').expect

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
    // given
    bot.chat("/make");

    // then
    bot.on('messagestr', (msg, _, json) => {
      if (msg !== "Player joined the game") {
        loginURL = json.extra[0].extra[0].extra[0].extra[0].clickEvent.value;
        expect(msg).to.equal("Click here to open a browser and start MAKE actions");
        done();
      }
    });
  });

  it("2 should create new command /demo", (done) => {
    const child = spawn('npm', ['run', 'playwright'], { env: { ...process.env, URL: loginURL } });
    child.stdout.pipe(process.stdout);
    child.stderr.pipe(process.stderr);
    child.on('exit', done);
  });

  it("3 should execute /demo", (done) => {
    // given
    bot.chat("/demo");

    // then
    bot.on('title', (msg) => {
      expect(msg).to.equal("{\"text\":\"automated test!\"}");
      done();
    });
  });
});