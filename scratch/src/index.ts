import * as EventBus from 'vertx3-eventbus-client';
import { Minecraft, HandType, Token } from '../../api/src/main/typescript/observable-wrapper';
import { JSEncrypt } from 'jsencrypt';

let ScratchExtensions: any;

(function (ext: any) {
    // Block and block menu descriptions <https://github.com/LLK/scratchx/wiki>
    var descriptor = {
        blocks: [
            // TODO Translate labels, like on https://github.com/jbaragry/mcpi-scratch/blob/master/mcpi-scratch.js
            ["h", "when %m.event", "when_event", "event"],
            ["h", "when %s right clicked", "when_entity", "entity"],
            ["h", "when inside %n %n %n, %n %n %n", "when_inside"],
            ["h", "Command /%s", "when_command", "demo"],
            ["w", "%s speak %s", "narrate", "entity", "text"],
            [" ", "/%s", "minecraftCommand", "command"],
            ["w", "title %s", "sendTitle", "Welcome!"],
            ["r", "last joined Player", "get_player_last_joined"],
            ["R", "Item held", "get_player_item_held"],
            ["r", "%m.item", "get_item_name", "Apple"]
        ],
        menus: {
            // NB: The order of the events here matters and is hard-coded in the registerHandler("mcs.events") below..
            event: ["Player joins"],
            item: ["Apple", "Beef", "Beetroot", "Boat", "Book", "Bow", "Bowl", "Bread",
                "Cactus", "Cake", "Carrot", "Cauldron", "Chicken", "Clock", "Cookie"]
        },
        url: "https://github.com/vorburger/minecraft-storeys-maker/"
    };

    var match,
        pl = /\+/g,  // Regex for replacing addition symbol with a space
        search = /([^&=]+)=?([^&]*)/g,
        decode = function (s) { return decodeURIComponent(s.replace(pl, " ")); },
        query = window.location.search.substring(1);

    var urlParams: any = {};
    while (match = search.exec(query))
        urlParams[decode(match[1])] = decode(match[2]);

    var minecraft: Minecraft;
    var eb; // the Vert.X EventBus
    var eventsReceived = {};
    var player_last_joined;
    var registeredConditions = new Set();
    var code: string;
    var token: Token;

    descriptor.menus.event.forEach(function (eventLabel) {
        eventsReceived[eventLabel] = false;
    });

    ext.sendTitle = (sendTitle: string, callback: Function) => {
        minecraft.showTitle(token, sendTitle).subscribe(() => callback(),
            (err: any) => console.log("sendTitle reply with error: ", err)
        );
    };
    ext.narrate = (entity: string, text: string, callback: Function) => {
        minecraft.narrate(code, entity, text).subscribe(() => callback(),
            (err: any) => console.log("narrate reply with error: ", err)
        );
    };
    ext.get_player_item_held = (callback: Function) => {
        minecraft.getItemHeld(code, HandType.MainHand).subscribe(result => callback(result),
            (err: any) => console.log("getItemHeld reply with error: ", err)
        );
    }
    ext.minecraftCommand = (commandToRun: string, callback: Function) => {
        minecraft.runCommand(code, commandToRun).subscribe(result => callback(result),
            (err: any) => console.log("runCommand reply with error: ", err)
        );
    };

    //
    // Hat Blocks Condition Events related stuff here...
    //
    ext.when_event = function (event) {
        const was = eventsReceived[event];
        // TODO if was is null/nil (?) then false (and remove pre-init)
        if (was === true) {
            console.log("when_event: was = " + was);
        }
        eventsReceived[event] = false;
        return was;
    };
    ext.whenCondition = (method: string, ...args: string[]) => {
        if (!registeredConditions.has(method + args)) {
            minecraft[method](token, args).subscribe(register => {
                register.on().subscribe(() => {
                    eventsReceived[method + args] = true;
                });
            });
            // We do this immediately after instead of inside the callback above, because we want it only once,
            // and callback may take a moment, but this gets called a lot; the chance that the send() failed is minimal.
            registeredConditions.add(method + args);
        }

        return ext.when_event(args);
    };

    ext.when_command = function (command) {
        return ext.whenCondition('whenCommand', command);
    };
    ext.when_inside = function (x1, y1, z1, x2, y2, z2) {
        return ext.whenCondition('whenInside', x1, y1, z1, x2, y2, z2);
    };
    ext.when_entity = function (entity) {
        return ext.whenCondition('whenEntityRightClicked', entity);
    };
    ext.get_player_last_joined = function () {
        return player_last_joined;
    };
    ext.get_item_name = function (itemName) {
        // Support translations here too...
        return itemName;
    };

    // Cleanup function when the extension is unloaded
    ext._shutdown = function () {
        // TODO eb. has no close(); ?!
    };

    // Status reporting code
    // Return any message to be displayed as a tooltip.
    // Use this to report missing hardware, plugin or unsupported browser
    // Status values: 0 = error (red), 1 = warning (yellow), 2 = ready (green)
    ext._getStatus = function () {
        // TODO implement based on JS getScript loading and eventBus connection!
        return { status: 2, msg: "Ready" };
    };

    let crypt = new JSEncrypt(512);
    eb = new EventBus(urlParams.eventBusURL);
    eb.enableReconnect(true);
    minecraft = new Minecraft(eb);
    eb.onopen = function () {
        if (typeof (urlParams.code !== 'undefined')) {
            crypt.getKey(() => {
                minecraft.login(urlParams.code, crypt.getPublicKeyB64()).subscribe(response => {
                    console.log("Logging in...", response);
                    const id = crypt.decrypt(response.secret);
                    const key = response.key;
                    crypt = new JSEncrypt();
                    crypt.setPublicKey(key);
                    code = crypt.encrypt(id);
                    token = { loginCode: code };
                    console.log("New style Token", token);

                    minecraft.whenPlayerJoins(token).subscribe(result => this.player_last_joined = result.player);
                }, err =>
                console.log("login reply with error: ", err)
            );
        });
    }

        eb.send("mcs.actions", { "action": "ping" });
        // TODO await "PONG" reply, and set status green
    };
    eb.onclose = function () {
        console.log("Vert.x Event Bus Connection Close");
    };

    // Register the extension
    (<any>window).ScratchExtensions.register("Minecraft", descriptor, ext);

})({});
