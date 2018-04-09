import { Minecraft } from "./minecraft";

describe('Minecraft TypeScript API', () => {
    let minecraft : Minecraft;

    beforeAll(() => {
        minecraft = new Minecraft("TODO-Token");
    });

    it('set title', () => {
        minecraft.setTitle("hello, world")
        .then(() => expect(true).toBe(true)) // TODO is this needed or can it be removed? Otherwise, how does it know we're done?
        .catch(error => fail(error))
    });
})