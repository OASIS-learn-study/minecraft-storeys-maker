# Testing

    ./test

## Story DSL

1. `/story test`
1. Verify [`test.story`](../minecraft-server-test-data/config/storeys-web/stories/test.story) happened as expected

## Scratch 3.0

1. `/make` and click link from Chat, open Scratch UI, add Minecraft Extension from lower-right hand corner
1. Add
   * when / `test-scratch`
   * title `Hello from Scratch`
   * `Piggy` speaks `I like Scratch`
1. `/test-scratch` should work
1. Add
   * when `Piggy` right clicked
   * `Piggy` speaks `Oink!`
1. right clicking Piggy should work

## JavaScript

1. _TODO_, see [JS scripting](../scratch3-server/README.md#third-scripting-option)

## Clean Up

    ./clean

This WILL LOOSE anything that's [ignored and not commited to git](../minecraft-server-test-data/.gitignore)!!!
