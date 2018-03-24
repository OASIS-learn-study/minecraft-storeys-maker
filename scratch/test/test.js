
var callbackInvoked;

var scratchMinecraftExtension;

ScratchExtensions = {
    register: function(extensionName, descriptor, ext) {
        scratchMinecraftExtension = ext;
        console.log("ScratchX extension registered: " + !(scratchMinecraftExtension === undefined));
    }
};
