describe('ScratchX Custom Blocks Integration Tests', () => {
    it('ext must be defined', () => {
        expect((<any>window).ext).toBeDefined();
    });
})
