# Develop

Try [testing](testing.md) first.

Code Formatting is enforced by Checkstyle.  Matching Eclipse Preferences and Formatting are available for Import from [`config/eclipse`](config/eclipse).

To develop new extensions to our Vert.x-based Minecraft remote API, we often first (before doing above) start the main() method of the SeleniumTest (AKA `cd ../web/ && ../gradlew runWithJavaExec`), which starts the TestMinecraft implementation, use `npm start` to run the client, and then open http://localhost:3000/?eventBusURL=http://localhost:6060/eventbus, and try out things in the Browser's Console.
