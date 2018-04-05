# minecraft-storeys-maker Scratch integration

## For end-users

_TODO Describe how to use this w.o. build; just BT DL, or... from an instance we'll eventually host??_


## For developers

Build the entire project (not just the `scratch/` or `web/`) via `./gradlew build`.

Copy the `web/build/libs/web-1.0.0-SNAPSHOT-all.jar` into your [spongepowered.org](https://www.spongepowered.org) Minecraft, typically the `mods/` directory of a Vanilla server.

Connect to your Minecraft server with the Minecraft client GUI, and use the `/login` console command.  
This will reply with a link to http://scratchx.org in the Minecraft chat.
_TODO That link is currently hard-coded to rawgit.com/.../minecraft.js; we need to make it configurable to localhost for development!_

### OSGi

You can also hack away at the Minecraft plugin server side with Java code hot reloading using https://github.com/vorburger/ch.vorburger.minecraft.osgi.  This will reload the plugin when you re-build it.  You can use `./gradlew build --continuous` to have it rebuilt automatically on each change you make.  In this mode, you don't use the `web-*-all.jar`, but you `/osgi:install file:/...` for the two smaller `web/build/libs/web-1.0.0-SNAPSHOT.jar` and the `storeys/build/libs/storeys-1.0.0-SNAPSHOT.jar`, and put all dependencies into the server's `osgi/boot` directory:

    1_org.apache.felix.log-1.0.1.jar
    2_org.everit.osgi.loglistener.slf4j-1.0.0.jar
    3_guava-18.0.jar
    3_guava-19.0.jar
    3_guava-20.0.jar
    3_guava-22.0.jar
    4_ch.vorburger.osgi.gradle-1.0.0-SNAPSHOT.jar
    4_org.gradle.tooling.osgi-3.3.jar
    5_ch.vorburger.minecraft.osgi.dev-1.0.0-SNAPSHOT.jar
    6_jackson-annotations-2.9.0.jar
    6_jackson-core-2.9.0.jar
    6_jackson-databind-2.9.0.jar
    6_netty-buffer-4.1.16.Final.jar
    6_netty-codec-4.1.16.Final.jar
    6_netty-codec-dns-4.1.15.Final.jar
    6_netty-codec-http2-4.1.16.Final.jar
    6_netty-codec-http-4.1.16.Final.jar
    6_netty-codec-socks-4.1.16.Final.jar
    6_netty-common-4.1.16.Final.jar
    6_netty-handler-4.1.16.Final.jar
    6_netty-handler-proxy-4.1.16.Final.jar
    6_netty-resolver-4.1.16.Final.jar
    6_netty-resolver-dns-4.1.15.Final.jar
    6_netty-transport-4.1.16.Final.jar
    6_vertx-auth-common-3.5.0.jar
    6_vertx-bridge-common-3.5.0.jar
    6_vertx-core-3.5.0.jar
    6_vertx-web-3.5.0.jar
