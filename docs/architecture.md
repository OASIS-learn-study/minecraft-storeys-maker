# Architecture

## Java Code

* `api/` is Storeys' Remote JavaScript with Vert.x API client
* `api-jvm/` is Storeys' Local JVM API for in-process Local JavaScript scripts (no Vert.x) & Java. It should only depend on the Sponge API, nothing else.
* `api-jvm-impl/` implements the `api-jvm/` API. It should only depend on `api-jvm` (and on `storeys`, later at the _grand inversion_ when we flip it upside down)
* `example/` is a simple sample plugin written in Java. It should only depend on `api-jvm`, nothing else.
* `engine/` will be an interactive dialogs runtime, useable both in Minecraft and standalone
* `storeys/` is the original core project and includes the `/narrate` command and `.story` DSL with `/story` _(TODO factor out Story DSL into `dsl/` module)_
* `test-utils/` is a minor technical utility for classpath duplication detection
* `web/` implements `api/` with a Vert.x server back-end for (a) Remote Scratch, (b) Scratch Server, (c) hand-written Remote JS (running both b+c within Node.JS)

## JavaScript Code

* `blockly/` is the Blockly UI. This generates JS code in the browser, and pushes that to the server via HTTP.
  On the server, new and changed JS files are detected and re-loaded and executed in-process of the Minecraft server.

## Infrastructure

* `minecraft-server/` is used by the `Dockerfile` to create the Minecraft server container image
* `minecraft-server-test-data/` is used by the `./test` script, see [testing docs](docs/testing.md)

## Other

* `logo/` has this project's graphical identity (courtesy of teneresa@)
