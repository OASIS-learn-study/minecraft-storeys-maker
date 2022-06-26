const mineflayer = require('mineflayer');
const { spawn } = require('node:child_process');

const TIMEOUT = 10 * 1000;

const createBot = (command) => {
  const bot = mineflayer.createBot({
    host: "localhost",
    port: 25565
  });
  bot.on("login", () => {
    bot.chat(command);
  });
  return bot;
}

const handleError = (bot, done) => {
  bot.on("error", (e) => {
    done(e);
  });
}

describe("Storeys plugin test", () => {
  beforeAll(() => {
    // Prevent jest from complaining about an import after the test is done (You are trying to `import` a file after the Jest environment has been torn down.)
    jest.useFakeTimers('legacy');
    console.log = () => { };
  });

  let bot;


  afterAll(() => bot.quit());

  test("1 should connect to minecraft server and execute /make", (done) => {
    // given
    const bot = createBot("/make");

    // then
    bot.on('messagestr', (msg, _, json) => {
      if (msg !== "Player joined the game") {
        loginURL = json.extra[0].extra[0].extra[0].extra[0].clickEvent.value;
        expect(msg).toEqual("Click here to open Scratch and MAKE actions");
        done();
        bot.quit();
      }
    });

    handleError(bot, done);
  }, TIMEOUT);

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
      bot.quit();
    });

    handleError(bot, done);
  }, TIMEOUT);
});
