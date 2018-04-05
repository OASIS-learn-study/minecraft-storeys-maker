# minecraft-storeys-maker Scratch integration

## For end-users

_TODO Describe how to use this w.o. build; just BT DL, or... from an instance we'll eventually host??_


## For developers

Build the entire project (not just the `scratch/` or `web/`) via `./gradlew build`.

Copy the `web/build/libs/web-1.0.0-SNAPSHOT-all.jar` into your [spongepowered.org](https://www.spongepowered.org) Minecraft, typically the `mods/` directory of a Vanilla server.

Connect to your Minecraft server with the Minecraft client GUI, and use the `/login` console command.  
This will reply with a link to http://scratchx.org in the Minecraft chat.
_TODO That link is currently hard-coded to rawgit.com/.../minecraft.js; we need to make it configurable to localhost for development!_

You can also hack away at the Minecraft plugin server side with Java code hot reloading using https://github.com/vorburger/ch.vorburger.minecraft.osgi.
Use the `/osgi:install file:/.../web/build/libs/web-1.0.0-SNAPSHOT-all.jar` to re-load the plugin when you re-build it;
use `./gradlew build --continuous` to have it rebuilt automatically on each change you make.
