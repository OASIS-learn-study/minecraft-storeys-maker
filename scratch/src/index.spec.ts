import { MinecraftProvider, HandType, ItemType, Minecraft } from "../../api/src/main/typescript/observable-wrapper";

describe('ScratchX Custom Blocks Integration Tests', () => {
    let minecraft: Minecraft;

    beforeAll(async done => {
        minecraft = await new MinecraftProvider('http://localhost:6060', 'dummy').connect();
        done();
    });

    it('should connect with the eventbus', () => {
        expect(minecraft).toBeDefined();
        expect(minecraft.loggedInPlayer).toBeDefined();
    });

    it('should get the Item held', async done => {
        const item = await minecraft.getItemHeld('dummy', HandType.MainHand);
        expect(item).toEqual(ItemType.Apple);
        done();
    });

    it('should remove apple form inventory', async done => {
        await minecraft.addRemoveItem('dummy', 1, ItemType.Apple)
        done();
    });

    // it('should respond to whenInside event', async done => {
    //     const registration = await minecraft.whenEntityRightClicked("joe");
    //     registration.on().subscribe(async response => {
    //         await minecraft.showTitle(response.playerUUID, "hello my little friend");
    //         done();
    //     })
    // });
})
