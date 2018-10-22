import { MinecraftProvider, HandType, ItemType, Minecraft } from "../../api/src/main/typescript/observable-wrapper";


describe('ScratchX Custom Blocks Integration Tests', () => {
    this.minecraft;
    beforeAll(done => {
        new MinecraftProvider().connect('http://localhost:6060/eventbus', 'dummy')
            .subscribe((minecraft: Minecraft) => {
                this.minecraft = minecraft;
                done();
            });
    });

    it('should connect with the eventbus', () => {
        expect(this.minecraft).toBeDefined();
    });

    it('should get the Item held', done => {
        this.minecraft.getItemHeld(HandType.MainHand).subscribe(item => {
            expect(item).toEqual(ItemType.Apple);
            done();
        });
    });

    it('should remove apple form inventory', done => {
        this.minecraft.addRemoveItem(1, ItemType.Apple).subscribe(() => done());
    })
})
