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

describe("Storeys plugin test", () => {
  let bot;

  const handleError = (done) => {
    bot.on("error", (e) => {
      done(e);
    });
  }
  
  afterEach(() => bot.quit());

  test("should connect to minecraft server and execute /make", (done) => {
    // given
    bot = createBot("/make");

    // then
    bot.on('messagestr', (msg) => {
      if (msg !== "Player joined the game") {
        expect(msg).toEqual("Click here to open Scratch and MAKE actions");
        done();
      }
    });

    handleError(done);
  }, TIMEOUT);

  test("should execute /new", (done) => {
    // given
    bot = createBot("/new");

    // then
    bot.on('title', (msg) => {
      expect(msg).toEqual("{\"text\":\"Hello\"}");
      done();
    });

    handleError(done);
  }, TIMEOUT);
});
