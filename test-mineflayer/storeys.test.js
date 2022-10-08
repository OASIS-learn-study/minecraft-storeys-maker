const mineflayer = require('mineflayer');

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

  it("should connect to minecraft server and execute /make", (done) => {
    // given
    bot.chat("/make");

    // then
    bot.on('messagestr', (msg, _, json) => {
      if (msg !== "Player joined the game") {
        expect(msg).to.equal("Click here to open Scratch and MAKE actions");
        done();
      }
    });
  });

  it("should execute /new", (done) => {
    // given
    bot.chat("/new");

    // then
    bot.on('title', (msg) => {
      expect(msg).to.equal("{\"text\":\"Hello\"}");
      done();
    });
  });
});
