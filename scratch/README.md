# minecraft-storeys-maker Scratch integration

## For developers

Build the entire project (not just the `scratch/` or `web/`) via `./gradlew build`.

Copy the `web/build/libs/web-1.0.0-SNAPSHOT-all.jar` into your [spongepowered.org](https://www.spongepowered.org) Minecraft, typically the `mods/` directory of a Vanilla server.

Start a web server for the ScratchX extension:

    cd scratch
    npm start

Make sure you CLOSE the page that just opened on http://localhost:3000, because its only used for testing, but the Vert.X EventBus client on it will interfere with the one in the ScratchX page.

Start the Minecraft server, and pass the URL where the ScratchX extension is available now:

    java -Dstoreys_jsURL=http://localhost:3000/minecraft.scratchx.js -jar spongevanilla-*.jar

Connect to your Minecraft server with the Minecraft client GUI, and use the `/login` console command.
This will reply with a link to http://scratchx.org in the Minecraft chat.

To develop new extensions to our Vert.x-based Minecraft remote API, we often first (before doing above) start the main() method of the SeleniumTest, which starts the TestMinecraft implementation, use `npm start` as above to run the client, and then open http://localhost:3000/?eventBusURL=http://localhost:6060/eventbus, and try out things in the Browser's Console.

To only just test Scratch (v2) integration issues in dev, you can also `npm start` and then open
http://scratchx.org/?url=http%3A%2F%2Flocalhost%3A3000%2Fminecraft.scratchx.js (the EventBus won't work, but you can debug problems with blocks).


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
