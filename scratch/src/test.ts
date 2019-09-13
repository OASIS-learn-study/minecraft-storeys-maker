export class Test {
    private callbacksCalled: boolean[] = [];
    scratchMinecraftExtension: any;

    callback(index): Function {
        return () => {
            console.log('callback called ' + index);
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
    register: function (extensionName, descriptor, ext) {
        test.scratchMinecraftExtension = ext;
        console.log("ScratchX extension registered:", extensionName, ext !== undefined);
    }
};

// =======================================================================================
// TODO rewrite most of what is in SeleniumTest here... like this:

import { MinecraftProvider, HandType, ItemType } from '../../api/src/main/typescript/observable-wrapper';

export class Tester {
    failures = [];
    private done: boolean = false;

    test(): void {
        const urlParams = new URL(window.location.href).searchParams;
        new MinecraftProvider(urlParams.get('eventBusURL'), urlParams.get('code')).connect().then(minecraft => {
            minecraft.getItemHeld(minecraft.loggedInPlayer, HandType.MainHand).then(
                result => {
                    if (result !== ItemType.Apple) this.failures.push("getItemHeld expected Apple but actually got " + result);
                    this.done = true;
                    console.log('getItemHeld result', result);
                },
                err => {
                    this.failures.push(err);
                    this.done = true;
                    console.log('getItemHeld error', err);
                });
        })
    }

    isDone(): boolean {
        return this.done;
    }
}

(<any>window).Tester = Tester;
