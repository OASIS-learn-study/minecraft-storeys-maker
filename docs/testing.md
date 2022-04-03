# Testing

    ./test

## Story DSL

1. `/story test`
1. Verify [`test.story`](../minecraft-server-test-data/config/storeys-web/stories/test.story) happened as expected

## Scratch 3.0

First test that Eric's (!) saved project (which is in git) works:

1. Right click the "Piggy" and verify it say "Hello, my friend!"
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
1. _TODO `/demo` should still work, but currently doesn't, because of `IllegalArgumentException: A plugin may not register multiple commands for the same alias ('demo')` in log; this needs more thoughts in general._

## JavaScript

1. _TODO_, see [JS scripting](../scratch3-server/README.md#third-scripting-option)

## Troubleshooting

To see what's going on inside the container, use:

    podman exec -it storeys bash

## Clean Up

    ./clean

This WILL LOOSE anything that's [ignored and not commited to git](../minecraft-server-test-data/.gitignore)!!!
