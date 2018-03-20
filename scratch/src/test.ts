export class Test {
    private callbacksCalled: Function[] = [];
    scratchMinecraftExtension: any;

    callback(index): Function {
        if (!this.callbacksCalled[index]) {
            this.callbacksCalled[index] = () => console.log('callback called', index);
        }
        return this.callbacksCalled[index];
    }

    isCallbackCalled(index: string): boolean {
        return this.callbacksCalled[index] || false;
    }
}

let test = new Test();

(<any>window).ext = test;

(<any>window).ScratchExtensions = {
    register: function(extensionName, descriptor, ext) {
        test.scratchMinecraftExtension = ext;
        console.log("ScratchX extension registered: " + !(test.scratchMinecraftExtension === undefined));
    }
};
