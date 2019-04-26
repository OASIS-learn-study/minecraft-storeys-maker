import { MinecraftProvider, HandType, ItemType, Minecraft } from "../../api/src/main/typescript/observable-wrapper";

describe('ScratchX Custom Blocks Integration Tests', () => {
    let minecraft: Minecraft;

    beforeAll(done => {
        new MinecraftProvider().connect('http://localhost:6060/eventbus')
            .subscribe(result => {
                minecraft = result;
                minecraft.login('dummy').subscribe(result => done());
            });
    });

    it('should connect with the eventbus', () => {
        expect(minecraft).toBeDefined();
    });

    it('should get the Item held', done => {
        minecraft.getItemHeld('dummy', HandType.MainHand).subscribe(item => {
            expect(item).toEqual(ItemType.Apple);
            done();
        });
    });

    it('should remove apple form inventory', done => {
        minecraft.addRemoveItem('dummy', 1, ItemType.Apple).subscribe(() => done());
    });

    // it('should respond to whenInside event')
})
