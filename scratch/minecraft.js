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
    var eb; // the Vert.X EventBus

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

    ext.doToDo = function() {
        // alert("TODO Not yet implemented");
        console.log("TODO Implement function...");
    };

    ext.sendTitle = function(sendTitle) {
        eb.send("mcs.actions", { "action": "setTitle", "text": sendTitle });
    };

    ext.narrate = function(entity, text) {
        eb.send("mcs.actions", { "action": "narrate", "entity": entity, "text": text });
    };

    // Block and block menu descriptions <https://github.com/LLK/scratchx/wiki>
    var descriptor = {
        blocks: [
            // TODO Translate labels, like on https://github.com/jbaragry/mcpi-scratch/blob/master/mcpi-scratch.js
            [" ", "Title %s", "sendTitle"],
            // [" ", "Subtitle %s", "doToDo"],
            [" ", "%s speak %s", "narrate"],
            [" ", "/say %s", "doToDo"],
            [" ", "/%s", "doToDo"],
        ],
        url: "https://github.com/vorburger/minecraft-storeys-maker/"
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
                    // TODO integrate with ScratchX "hat blocks" when Minecraft events are received..
                    console.log("error: " + error);
                    console.log("message: " + JSON.stringify(message));
                });

                eb.send("mcs.actions", { "action": "ping" });
                // TODO await "PONG" reply, and set status green
            };

            // Register the extension
            ScratchExtensions.register("Minecraft", descriptor, ext);
        });
    });
})({});