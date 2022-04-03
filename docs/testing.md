# Testing

    ./test

## Story DSL

1. `/story test`
1. Verify [`test.story`](../minecraft-server-test-data/config/storeys-web/stories/test.story) happened as expected

## Scratch 3.0

1. Right click the "Piggy" and verify it say "Hello, my friend!"
1. type `/scratch_test` verify the title shown
1. `/make` and open the browser
1. _TODO_

## JavaScript

1. _TODO_, see [JS scripting](../scratch3-server/README.md#third-scripting-option)

## Troubleshooting

To see what's going on inside the container, use:

    podman exec -it storeys bash

## Clean Up

    ./clean

This WILL LOOSE anything that's [ignored and not commited to git](../minecraft-server-test-data/.gitignore)!!!
