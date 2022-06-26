const mineflayer = require('mineflayer');
const { spawn } = require('node:child_process');

const expect = require('chai').expect

describe("Storeys plugin test", () => {
  let bot;

  before((done) => {
    bot = mineflayer.createBot({
      host: "localhost",
      port: 25565
    });
    bot.on("login", done);
  })

  after(() => bot.end());

  let bot;


  afterAll(() => bot.quit());

  test("1 should connect to minecraft server and execute /make", (done) => {
    // given
    bot.chat("/make");

    // then
    bot.on('messagestr', (msg, _, json) => {
      if (msg !== "Player joined the game") {
        loginURL = json.extra[0].extra[0].extra[0].extra[0].clickEvent.value;
        done();
      }
    });
  });

  it("should execute /new", (done) => {
  test("2 should create new command /demo", (done) => {
    const child = spawn('npx', ['cypress', 'run', '--browser', 'chrome'], { env: { ...process.env, CYPRESS_URL: loginURL } });
    child.stdout.pipe(process.stdout);
    child.stderr.pipe(process.stderr);
    child.on('close', done);
  }, 2 * TIMEOUT);

  test("3 should execute /demo", (done) => {
    // given
    bot = createBot("/demo");

    // then
    bot.on('title', (msg) => {
      expect(msg).toEqual("{\"text\":\"automated test!\"}");
      done();
    });
  });
});
