const mineflayer = require('mineflayer');

describe("Storeys plugin test", () => {
  beforeAll(() => {
    // Prevent jest from complaining about an import after the test is done (You are trying to `import` a file after the Jest environment has been torn down.)
    jest.useFakeTimers('legacy');
    console.log = () => {};
  });

  test("should connect to minecraft server and execute /make", (done) => {
    // given
    const bot = mineflayer.createBot({
      host: 'localhost',
      port: 25565
    });

    // when
    bot.on("login", () => {
      bot.chat('/make');
    });

    // then
    bot.on('messagestr', (msg) => {
      if (msg !== "Player joined the game") {
        expect(msg).toEqual("Click here to open Scratch and MAKE actions");
        done();
        bot.quit();
      }
    });

    bot.on("error", (e) => {
      done(e);
    });
  }, 10 * 1000);
});