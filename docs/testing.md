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

## JavaScript

1. _TODO_, see [JS scripting](../scratch3-server/README.md#third-scripting-option)

## Troubleshooting

To see what's going on inside the container, use:

    podman exec -it storeys bash

## Clean Up

    ./clean

This WILL LOOSE anything that's [ignored and not commited to git](../minecraft-server-test-data/.gitignore)!!!
