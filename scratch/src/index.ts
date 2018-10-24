import { MinecraftProvider, Minecraft, HandType, Token, ItemType } from '../../api/src/main/typescript/observable-wrapper';

let ScratchExtensions: any;

(function (ext: any) {
    // Block and block menu descriptions <https://github.com/LLK/scratchx/wiki>
    const descriptor = {
        blocks: [
            // TODO Translate labels, like on https://github.com/jbaragry/mcpi-scratch/blob/master/mcpi-scratch.js
            ["h", "when %m.event", "when_event", "event"],
            ["h", "when %s right clicked", "when_entity", "entity"],
            ["h", "when inside %s", "when_inside", "name"],
            ["h", "Command /%s", "when_command", "demo"],
            ["w", "%s speak %s", "narrate", "entity", "text"],
            ["w", "/%s", "minecraftCommand", "command"],
            ["w", "title %s", "sendTitle", "Welcome!"],
            ["w", "add / remove %n item %m.item", "addRemoveItem", "amount", "item"],
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

    const urlParams = new URL(window.location.href).searchParams;
    let eventsReceived = {};
    let player_last_joined;
    let registeredConditions = new Set();

    descriptor.menus.event.forEach(function (eventLabel) {
        eventsReceived[eventLabel] = false;
    });

    new MinecraftProvider().connect(urlParams.get('eventBusURL'), urlParams.get('code')).subscribe(minecraft => {
        ext.sendTitle = (sendTitle: string, callback: Function) => {
            minecraft.showTitle(sendTitle).subscribe(() => callback(),
                (err: any) => console.log("sendTitle reply with error: ", err)
            );
        };
        ext.narrate = (entity: string, text: string, callback: Function) => {
            minecraft.narrate(entity, text).subscribe(() => callback(),
                (err: any) => console.log("narrate reply with error: ", err)
            );
        };
        ext.get_player_item_held = (callback: Function) => {
            minecraft.getItemHeld(HandType.MainHand).subscribe(result => callback(result),
                (err: any) => console.log("getItemHeld reply with error: ", err)
            );
        }
        ext.minecraftCommand = (commandToRun: string, callback: Function) => {
            minecraft.runCommand(commandToRun).subscribe(() => callback(),
                (err: any) => console.log("runCommand reply with error: ", err)
            );
        };
        ext.addRemoveItem = (amount: number, item: ItemType, callback: Function) => {
            minecraft.addRemoveItem(amount, item).subscribe(() => callback(),
                (err: any) => console.log("addRemoveItem reply with error: ", err)
            );
        }

        //
        // Hat Blocks Condition Events related stuff here...
        //
        ext.when_event = function (event) {
            const was = eventsReceived[event];
            eventsReceived[event] = false;
            return was || false;
        };
        ext.whenCondition = (method: string, ...args: string[]) => {
            if (!registeredConditions.has(method + args)) {
                registeredConditions.add(method + args);
                minecraft[method].apply(minecraft, args).subscribe(register => {
                    register.on().subscribe(() => {
                        eventsReceived[method + args] = true;
                    });
                });
            }
          return ext.when_event(method + args);
        };

        ext.when_command = function (command) {
          return ext.whenCondition('whenCommand', command);
        };
        ext.when_inside = function (name) {
          return ext.whenCondition('whenInside', name);
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

        minecraft.whenPlayerJoins().subscribe(result => {
            player_last_joined = result.player;
            eventsReceived["Player joins"] = true;
        });

        // Register the extension
        (<any>window).ScratchExtensions.register("Minecraft", descriptor, ext);
    });
})({});
