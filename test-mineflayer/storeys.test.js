const mineflayer = require('mineflayer');

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

  test("should connect to minecraft server and execute /make", (done) => {
    // given
    const bot = createBot("/make");

    // then
    bot.on('messagestr', (msg) => {
      if (msg !== "Player joined the game") {
        expect(msg).toEqual("Click here to open Scratch and MAKE actions");
        done();
        bot.quit();
      }
    });

    handleError(bot, done);
  }, TIMEOUT);

  test("should execute /new", (done) => {
    // given
    const bot = createBot("/new");

    // then
    bot.on('title', (msg) => {
      expect(msg).toEqual("{\"text\":\"Hello\"}");
      done();
      bot.quit();
    });

    handleError(bot, done);
  }, TIMEOUT);
});