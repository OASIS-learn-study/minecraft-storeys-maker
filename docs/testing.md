# Testing

Run [`./test`](../test), then open your Minecraft client to connect to `localhost` and try the following.

## Story DSL

1. `/story test`
1. Verify [`test.story`](../minecraft-server-test-data/config/storeys-web/stories/test.story) happened as expected

## Scratch 3.0

First test that Eric's (!) saved project (which is in git) works:

1. Right click _Piggy_ and verify it says _"Hello, my friend!"_
1. type `/scratch_test` verify the title shown

Now test that Michael can make a new project:

1. `/make` and click the link in the Minecraft Chat to open the Scratch UI in the browser
1. Add
   * when / `demo`
   * `Piggy` speaks `I like Scratch`
1. `/demo` should work
1. click _Save_
1. close Browser
1. _TODO `/demo` should still work, but currently doesn't anymore (because project JSON wasn't automagically moved from `/minecraft-server-test-data/config/storeys-web/working/` to `storeys-web/scratch/`)_
1. `/make` again
1. Verify that previously created blocks still appear
1. _TODO bug #310 `/demo` should still work, but currently doesn't, because of `IllegalArgumentException: A plugin may not register multiple commands for the same alias ('demo')` in log; this needs more thoughts in general._

## JavaScript remote via Vert.x (original)

Test [JS scripting](../scratch3-server/README.md#third-scripting-option):

1. Right click _Chestnut_ the horse
1. Verify [`test.js`](../minecraft-server-test-data/config/storeys-web/scripts/test.js) happened as expected

The following doesn't work yet, but should after bug #310 is resolved:

1. `nano minecraft-server-test-data/config/storeys-web/scripts/test.js` and change the title text
1. Right click _Chestnut_ the horse
1. Verify new title is shown

## JavaScript local (new)

1. `/story js`
1. Verify [`js.story`](../minecraft-server-test-data/config/storeys-web/stories/js.story) happened as expected

## Troubleshooting

To see what's going on inside the container, use:

    podman exec -it storeys bash

## Clean Up

Run [`./clean`](../clean), this WILL LOOSE any in-game world changes
that are [ignored and not commited to git](../minecraft-server-test-data/.gitignore)!
