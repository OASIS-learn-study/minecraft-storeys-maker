import * as EventBus from 'vertx3-eventbus-client';
import { Minecraft } from '../../api/build/classes/java/main/Minecraft-ts/minecraft-proxy';
// TODO remove, probably; because already import'd in Minecraft:
// import { CommandRegistration } from '../../api/build/classes/java/main/Minecraft-ts/command_registration-proxy';
import { JSEncrypt } from 'jsencrypt';

let ScratchExtensions: any;

(function(ext: any) {
    // Block and block menu descriptions <https://github.com/LLK/scratchx/wiki>
    var descriptor = {
        blocks: [
            // TODO Translate labels, like on https://github.com/jbaragry/mcpi-scratch/blob/master/mcpi-scratch.js
            ["h", "when %m.event",      "when_event",             "event"],
            ["h", "when %s %m.interaction", "when_entity",        "entity", "right clicked"],
            ["h", "when inside %n %n %n, %n %n %n", "when_inside"],
            ["h", "Command /%s",        "when_command",           "demo"],
            ["w", "%s speak %s",        "narrate",                "entity", "text"],
            [" ", "/%s",                "minecraftCommand",       "command"],
            ["w", "title %s",           "sendTitle",              "Welcome!"],
            ["r", "last joined Player", "get_player_last_joined"],
            ["r", "Item held", "get_player_item_held"],
            ["r", "%m.item", "get_item_name", "Apple"]
        ],
        menus: {
            // NB: The order of the events here matters and is hard-coded in the registerHandler("mcs.events") below..
            event: ["Player joins"],
            interaction: [/*"left clicked", */ "right clicked"],
            item: ["Apple", "Bed", "Beef", "Beetroot", "Boat", "Book", "Bow", "Bowl", "Bread", "Bucket",
                "Cactus", "Cake", "Carrot", "Cauldron", "Chicken", "Clock", "Cookie",
                "Door", "Mushroom"]
        },
        url: "https://github.com/vorburger/minecraft-storeys-maker/"
    };

    var match,
        pl     = /\+/g,  // Regex for replacing addition symbol with a space
        search = /([^&=]+)=?([^&]*)/g,
        decode = function (s) { return decodeURIComponent(s.replace(pl, " ")); },
        query  = window.location.search.substring(1);

    var urlParams: any = {};
    while (match = search.exec(query))
    urlParams[decode(match[1])] = decode(match[2]);

    var minecraft : Minecraft;
    var eb; // the Vert.X EventBus
    var eventsReceived = { };
    var player_last_joined;
    var registeredConditions = new Set();
    var code;

    descriptor.menus.event.forEach(function(eventLabel) {
        eventsReceived[eventLabel] = false;
    });

    ext.sendTitle = function(sendTitle: string, callback: Function) {
        minecraft.showTitle(code, sendTitle, (err: any, result: any) => {
            // TODO edewit: factor this out into a ../../api/src/api-utils.ts (?) usable from all functions here as well as scratch3/
            // see https://github.com/vorburger/minecraft-storeys-maker/issues/59 for some more ideas
            if (err) {
                console.log("sendTitle reply with error: ", err);
            } else {
                callback();
            }
        });
    };
    ext.narrate = function(entity: string, text: string, callback: Function) {
        minecraft.narrate(code, entity, text, (err: any) => {
            if (err) {
                console.log("narrate reply with error: ", err);
            } else {
                callback();
            }
        })
    };
    ext.minecraftCommand = function(command) {
        eb.send("mcs.actions", { "action": "command", "command": command, "code": code });
    };

    //
    // Hat Blocks Condition Events related stuff here...
    //
    ext.eventReceived = function(message) {
        console.log("Vert.x Event Bus received message: " + JSON.stringify(message));
        // because the descriptor.menus.event[] will be translated, we have to "map" these:
        if (message.body.event == "playerJoined") {
            player_last_joined = message.body.player;
            eventsReceived[descriptor.menus.event[0]] = true; // event[0] is "Player joins"
        } else {
            // This is for all the whenCondition events...
            eventsReceived[message.body.event] = true;
            console.log("eventsReceived[" + message.body.event + "] = true");
        }
    };
    ext.when_event = function(event) {
        var was = eventsReceived[event];
        // TODO if was is null/nil (?) then false (and remove pre-init)
        if (was == true) {
            console.log("when_event: was = " + was);
        }
        eventsReceived[event] = false;
        return was;
    };
    ext.whenCondition = function(condition) {
        if (!registeredConditions.has(condition)) {
            eb.send("mcs.actions", { "action": "registerCondition", "condition": condition, "code": code }, function(reply) {
            });
            // We do this immediately after instead of inside the callback above, because we want it only once,
            // and callback may take a moment, but this gets called a lot; the chance that the send() failed is minimal.
            registeredConditions.add(condition);
        } else {
            return ext.when_event(condition);
        }
    };
    ext.when_command = function(command) {
        return ext.whenCondition("newCmd" + command);
    };
    ext.when_inside = function(x1, y1, z1, x2, y2, z2) {
        return ext.whenCondition("myPlayer_inside_" + x1 + "/" + y1 + "/" + z1 + "/" + x2 + "/" + y2 + "/" + z2 + "/");
    };
    ext.when_entity = function(entity, interaction) {
        return ext.whenCondition("entity_interaction:" + entity + "/" + interaction);
    };
    ext.get_player_last_joined = function() {
        return player_last_joined;
    };
    ext.get_item_name = function(itemName) {
        // Support translations here too...
        return itemName;
    };

    // Cleanup function when the extension is unloaded
    ext._shutdown = function() {
        // TODO eb. has no close(); ?!
    };

    // Status reporting code
    // Return any message to be displayed as a tooltip.
    // Use this to report missing hardware, plugin or unsupported browser
    // Status values: 0 = error (red), 1 = warning (yellow), 2 = ready (green)
    ext._getStatus = function() {
        // TODO implement based on JS getScript loading and eventBus connection!
        return { status: 2, msg: "Ready" };
    };

    var crypt = new JSEncrypt(512);
    eb = new EventBus(urlParams.eventBusURL);
    eb.enableReconnect(true);
    eb.onopen = function() {
        eb.registerHandler("mcs.events", function (error, message) {
            if (error != null) {
                console.log("Vert.x Event Bus received error: " + error);
            } else {
                ext.eventReceived(message);
            }
        });

        if (typeof (urlParams.code !== 'undefined')) {
            crypt.getKey(function() {
                eb.send("mcs.actions", { action: "login", token: urlParams.code, key: crypt.getPublicKeyB64() }, (error, message) => {
                    if (!error && message.body) {
                        console.log("Logging in... secret", message.body.secret);
                        var id = crypt.decrypt(message.body.secret);
                        var key = message.body.key;
                        crypt = new JSEncrypt();
                        crypt.setPublicKey(key);
                        code = crypt.encrypt(id);
                    }
                });
            });
        }

        eb.send("mcs.actions", { "action": "ping" });
        // TODO await "PONG" reply, and set status green
    };
    eb.onclose = function() {
        console.log("Vert.x Event Bus Connection Close");
    };

    minecraft = new Minecraft(eb, "ch.vorburger.minecraft.storeys");

    // Register the extension
    (<any>window).ScratchExtensions.register("Minecraft", descriptor, ext);

    // TODO [re]move this; it's just the first iteration of a test:
    // minecraft.newCommand(ext.scratchMinecraftExtension.code, "test",
    // (err:any, result:any) => {
    //   if (err) {
    //       console.log("newCommand reply with error: ", err);
    //   } else {
    //     console.log("newCommand registered OK, now adding CommandRegistration");
    //     result.on();
    //   });
})({});
