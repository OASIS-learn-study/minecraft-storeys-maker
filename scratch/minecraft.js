(function(ext) {
    // Cleanup function when the extension is unloaded
    ext._shutdown = function() {};

    // Status reporting code
    // Return any message to be displayed as a tooltip.
    // Use this to report missing hardware, plugin or unsupported browser
    // Status values: 0 = error (red), 1 = warning (yellow), 2 = ready (green)
    ext._getStatus = function() {
        return {status: 2, msg: "Ready"};
    };

    ext.doToDo = function() {
        alert("TODO Not yet implemented");
    };

    // Block and block menu descriptions <https://github.com/LLK/scratchx/wiki>
    var descriptor = {
        blocks: [
            // TODO Translate labels, like on https://github.com/jbaragry/mcpi-scratch/blob/master/mcpi-scratch.js
            [" ", "Title %s", "doToDo"],
            [" ", "Subtitle %s", "doToDo"],
            [" ", "%s speak %s", "doToDo"],
            [" ", "/say %s", "doToDo"],
            [" ", "/%s", "doToDo"],
        ],
        url: "https://github.com/vorburger/minecraft-storeys-maker/"
    };

    // Register the extension
    ScratchExtensions.register("Minecraft", descriptor, ext);
})({});