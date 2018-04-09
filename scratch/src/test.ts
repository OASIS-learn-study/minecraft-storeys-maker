export class Test {
    private callbacksCalled: boolean[] = [];
    scratchMinecraftExtension: any;

    callback(index): Function {
        return () => {
            console.log('callback called', index);
            this.callbacksCalled[index] = true;
        }
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
        console.log("ScratchX extension registered:", extensionName, ext !== undefined);
    }
};
