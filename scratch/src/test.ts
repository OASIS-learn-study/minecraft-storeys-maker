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
    register: function(extensionName, descriptor, ext) {
        test.scratchMinecraftExtension = ext;
        console.log("ScratchX extension registered:", extensionName, ext !== undefined);
    }
};

// =======================================================================================
// TODO rewrite most of what is in SeleniumTest here... like this:

import * as EventBus from 'vertx3-eventbus-client';
import { Minecraft, Token, HandType, ItemType } from '../../api/src/main/typescript/observable-wrapper';

export class Tester {
    failures = new Set<string>();
    private done: boolean = false;

    test(): void {
        // copy/paste from index.ts - sorry! ;)
        var match,
            pl = /\+/g,  // Regex for replacing addition symbol with a space
            search = /([^&=]+)=?([^&]*)/g,
            decode = function (s) { return decodeURIComponent(s.replace(pl, " ")); },
            query = window.location.search.substring(1);
        var urlParams: any = {};
        while (match = search.exec(query))
            urlParams[decode(match[1])] = decode(match[2]);
        var eb = new EventBus(urlParams.eventBusURL);
        var minecraft = new Minecraft(eb);

        minecraft.getItemHeld('', HandType.MainHand).subscribe(
            result => {
                if (result != ItemType.Apple) this.failures.add("getItemHeld expected Apple but actually got " + result);
                this.done = true;
            },
            err => {
                this.failures.add(err);
                this.done = true;
            }
        )
    }

    isDone(): boolean {
        return this.done;
    }
}

let atester = new Tester();
(<any>window).tester = atester;
