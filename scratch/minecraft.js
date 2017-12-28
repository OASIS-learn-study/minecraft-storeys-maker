/*
 * ch.vorburger.minecraft.storeys
 *
 * Copyright (C) 2016 - 2017 Michael Vorburger.ch <mike@vorburger.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function(ext) {
    // Block and block menu descriptions <https://github.com/LLK/scratchx/wiki>
    var descriptor = {
        blocks: [
            // TODO Translate labels, like on https://github.com/jbaragry/mcpi-scratch/blob/master/mcpi-scratch.js
            ["h", "when %m.event",      "when_event",             "event"],
            ["h", "when inside %n %n %n, %n %n %n", "when_inside"],
            ["r", "last joined Player", "get_player_last_joined"],
            ["w", "title %s",           "sendTitle",              "Welcome!"],
            ["w", "%s speak %s",        "narrate",                "entity", "text"],
            [" ", "/%s",                "minecraftCommand",       "command"],
            // [" ", "/say %s", "TODO"],
        ],
        menus: {
            // NB: The order of the events here matters and is hard-coded in the registerHandler("mcs.events") below..
            event: ["Player joins"]
        },
        url: "https://github.com/vorburger/minecraft-storeys-maker/"
    };

    var eb; // the Vert.X EventBus
    var eventsReceived = { };
    var player_last_joined;
    var registeredConditions = new Set();

    descriptor.menus.event.forEach(function(eventLabel) {
        eventsReceived[eventLabel] = false;
    });

    ext.sendTitle = function(sendTitle, callback) {
        eb.send("mcs.actions", { "action": "setTitle", "text": sendTitle }, function(reply) {
            callback();
        });
    };
    ext.narrate = function(entity, text, callack) {
        eb.send("mcs.actions", { "action": "narrate", "entity": entity, "text": text }, function(reply) {
            callack();
        });

    };
    ext.minecraftCommand = function(command) {
        eb.send("mcs.actions", { "action": "command", "command": command });
    };

    //
    // Hat Blocks Condition Events related stuff here...
    //
    ext.eventReceived = function(message) {
        console.log("Vert.x Event Bus received message: " + JSON.stringify(message));
        // because the descriptor.menus.event[] will be translated, we have to "map" these:
        if (message.body.event == "playerJoined") {
            eventsReceived[descriptor.menus.event[0]] = true; // event[0] is "Player joins"
            player_last_joined = message.body.player;
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
            eb.send("mcs.actions", { "action": "registerCondition", "condition": condition }, function(reply) {
            });
            // We do this immediately after instead of inside the callback above, because we want it only once,
            // and callback may take a moment, but this gets called a lot; the chance that the send() failed is minimal.
            registeredConditions.add(condition);
        } else {
            return ext.when_event(condition);
        }
    };
    ext.when_inside = function(x1, y1, z1, x2, y2, z2) {
        return ext.whenCondition("myPlayer_inside_" + x1 + "/" + y1 + "/" + z1 + "/" + x2 + "/" + y2 + "/" + z2 + "/");
    };
    ext.get_player_last_joined = function() {
        return player_last_joined;
    };

    // Cleanup function when the extension is unloaded
    ext._shutdown = function() {};

    // Status reporting code
    // Return any message to be displayed as a tooltip.
    // Use this to report missing hardware, plugin or unsupported browser
    // Status values: 0 = error (red), 1 = warning (yellow), 2 = ready (green)
    ext._getStatus = function() {
        // TODO implement based on JS getScript loading and eventBus connection!
        return { status: 2, msg: "Ready" };
    };

    $.ajaxSetup({
        cache: true
    });

    ext.loadScript = function(url, onDone) {
        $.getScript(url)
            .done(function(script, textStatus) {
                console.log("done loading " + url + "; status=" + textStatus);
                onDone();
            })
            .fail(function(jqxhr, settings, exception) {
                var msg = "FAILED loading " + url + "; status=" + exception;
                console.log(msg);
                alert(msg);
            });
    };

    ext.loadScript("http://cdn.jsdelivr.net/sockjs/0.3.4/sockjs.min.js", function() {
        ext.loadScript("https://cdnjs.cloudflare.com/ajax/libs/vertx/3.5.0/vertx-eventbus.min.js", function() {

            // TODO url must be made configurable
            eb = new EventBus("http://localhost:8080/eventbus");
            eb.enableReconnect(true);
            eb.onopen = function() {
                eb.registerHandler("mcs.events", function (error, message) {
                    if (error != null) {
                        console.log("Vert.x Event Bus received error: " + error);
                    } else {
                        ext.eventReceived(message);
                    }
                });

                eb.send("mcs.actions", { "action": "ping" });
                // TODO await "PONG" reply, and set status green
            };
            eb.onclose = function() {
                console.log("Vert.x Event Bus Connection Close");
            };

            // Register the extension
            ScratchExtensions.register("Minecraft", descriptor, ext);
        });
    });
})({});